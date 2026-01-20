/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ReservedSequence;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.ReservedSequenceRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.axelor.db.tenants.TenantAware;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link SequenceReservationService}.
 *
 * <p>This service coordinates sequence number generation with transaction-aware lifecycle
 * management. It uses {@link SequenceIncrementExecutor} for isolated increment operations and
 * {@link SequenceComputationService} for number formatting.
 */
@Singleton
@ThreadSafe
public class SequenceReservationServiceImpl implements SequenceReservationService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int DEFAULT_TIMEOUT_SECONDS = 5;

  private final SequenceIncrementExecutor incrementExecutor;
  private final SequenceComputationService computationService;
  private final SequenceVersionRepository sequenceVersionRepository;
  private final ReservedSequenceRepository reservedSequenceRepository;
  private final AppBaseService appBaseService;
  private final ExecutorService reservationExecutor;

  @Inject
  public SequenceReservationServiceImpl(
      SequenceIncrementExecutor incrementExecutor,
      SequenceComputationService computationService,
      SequenceVersionRepository sequenceVersionRepository,
      ReservedSequenceRepository reservedSequenceRepository,
      AppBaseService appBaseService) {
    this.incrementExecutor = incrementExecutor;
    this.computationService = computationService;
    this.sequenceVersionRepository = sequenceVersionRepository;
    this.reservedSequenceRepository = reservedSequenceRepository;
    this.appBaseService = appBaseService;
    this.reservationExecutor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = new Thread(r, "sequence-reservation-executor");
              t.setDaemon(true);
              return t;
            });
    log.info("SequenceReservationService initialized with single-thread executor");
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String reserveSequenceNumber(
      Sequence sequence, LocalDate refDate, Class<?> objectClass, String fieldName, Model model)
      throws AxelorException {

    log.trace("Reserving sequence number for sequence {} at date {}", sequence.getId(), refDate);

    // Step 1: Try to reuse a RELEASED sequence
    ReservedSequence released = findReleasedSequence(sequence, refDate);
    if (released != null) {
      log.trace("Reusing released reservation {}", released.getId());
      return reuseReleasedSequence(released, objectClass, fieldName);
    }

    // Step 2: Increment the sequence in an isolated transaction
    IncrementResult incrementResult = incrementExecutor.incrementAndGet(sequence.getId(), refDate);

    // Step 3: Fetch the sequence version (read-only, no lock needed)
    SequenceVersion sequenceVersion =
        sequenceVersionRepository.find(incrementResult.getSequenceVersionId());

    // Step 4: Compute the formatted sequence number
    String sequenceNumber =
        computationService.computeSequenceNumber(
            sequenceVersion, sequence, refDate, model, incrementResult.getNextNum());

    // Step 5: Check for duplicates if configured
    if (shouldCheckDuplicates(objectClass, fieldName)) {
      checkSequenceNotExists(objectClass, fieldName, sequenceNumber, sequence);
    }

    // Step 6: Create reservation in an isolated transaction
    Long reservationId =
        createReservation(
            sequence,
            sequenceVersion,
            incrementResult.getNextNum(),
            sequenceNumber,
            objectClass,
            fieldName);

    // Step 7: Register transaction synchronization for commit/rollback handling
    registerTransactionCallback(reservationId);

    log.debug("Reserved sequence number {} with reservation {}", sequenceNumber, reservationId);

    return sequenceNumber;
  }

  @Override
  public void confirmReservation(Long reservationId) {
    ReservedSequence reservation = reservedSequenceRepository.find(reservationId);
    if (reservation != null
        && reservation.getStatus() == ReservedSequenceRepository.STATUS_PENDING) {
      reservation.setStatus(ReservedSequenceRepository.STATUS_CONFIRMED);
      reservedSequenceRepository.save(reservation);
      log.trace("Confirmed reservation {}", reservationId);
    }
  }

  @Override
  public void releaseReservation(Long reservationId) {
    ReservedSequence reservation = reservedSequenceRepository.find(reservationId);
    if (reservation != null
        && reservation.getStatus() == ReservedSequenceRepository.STATUS_PENDING) {
      reservation.setStatus(ReservedSequenceRepository.STATUS_RELEASED);
      reservedSequenceRepository.save(reservation);
      log.debug("Released reservation {}", reservationId);
    }
  }

  /**
   * Finds a RELEASED sequence that can be reused.
   *
   * @param sequence the sequence
   * @param refDate the reference date
   * @return a released reservation, or null if none available
   */
  protected ReservedSequence findReleasedSequence(Sequence sequence, LocalDate refDate) {
    // Find the sequence version for the reference date
    SequenceVersion version = sequenceVersionRepository.findByDate(sequence, refDate);
    if (version == null) {
      return null;
    }

    // Find oldest RELEASED reservation for this version with lock timeout
    return JPA
        .em()
        .createQuery(
            "SELECT rs FROM ReservedSequence rs "
                + "WHERE rs.sequence = :sequence "
                + "AND rs.sequenceVersion = :version "
                + "AND rs.status = :status "
                + "ORDER BY rs.reservedNum ASC",
            ReservedSequence.class)
        .setParameter("sequence", sequence)
        .setParameter("version", version)
        .setParameter("status", ReservedSequenceRepository.STATUS_RELEASED)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .setHint("javax.persistence.lock.timeout", 5000)
        .setFlushMode(FlushModeType.COMMIT)
        .setMaxResults(1)
        .getResultList()
        .stream()
        .findFirst()
        .orElse(null);
  }

  /**
   * Reuses a released sequence reservation.
   *
   * @param released the released reservation
   * @param objectClass the caller class
   * @param fieldName the caller field
   * @return the sequence number
   * @throws AxelorException if duplicate check fails
   */
  protected String reuseReleasedSequence(
      ReservedSequence released, Class<?> objectClass, String fieldName) throws AxelorException {

    String sequenceNumber = released.getGeneratedSequence();

    // Check for duplicates if configured
    if (shouldCheckDuplicates(objectClass, fieldName)) {
      checkSequenceNotExists(objectClass, fieldName, sequenceNumber, released.getSequence());
    }

    // Update the reservation to PENDING with new caller info
    released.setStatus(ReservedSequenceRepository.STATUS_PENDING);
    released.setReservedAt(LocalDateTime.now());
    released.setCallerClass(objectClass != null ? objectClass.getName() : null);
    released.setCallerField(fieldName);
    reservedSequenceRepository.save(released);

    // Register transaction callback
    registerTransactionCallback(released.getId());

    return sequenceNumber;
  }

  /**
   * Creates a new reservation in an isolated transaction.
   *
   * <p>This method uses an ExecutorService to truly isolate the transaction in a separate thread
   * with its own EntityManager, avoiding issues with cascade operations in the parent transaction.
   *
   * @param sequence the sequence
   * @param version the sequence version
   * @param reservedNum the reserved number
   * @param sequenceNumber the formatted sequence number
   * @param objectClass the caller class
   * @param fieldName the caller field
   * @return the reservation ID
   */
  protected Long createReservation(
      Sequence sequence,
      SequenceVersion version,
      Long reservedNum,
      String sequenceNumber,
      Class<?> objectClass,
      String fieldName) {

    final Long sequenceId = sequence.getId();
    final Long versionId = version.getId();
    final String callerClass = objectClass != null ? objectClass.getName() : null;

    // Use AtomicReference to capture result and exception from nested lambda
    AtomicReference<Long> resultRef = new AtomicReference<>();
    AtomicReference<Exception> exceptionRef = new AtomicReference<>();

    Callable<Void> callable =
        () -> {
          TenantAware tenantAware =
              new TenantAware(
                  () -> {
                    try {
                      ReservedSequence reservation = new ReservedSequence();
                      reservation.setSequence(JPA.find(Sequence.class, sequenceId));
                      reservation.setSequenceVersion(JPA.find(SequenceVersion.class, versionId));
                      reservation.setReservedNum(reservedNum);
                      reservation.setGeneratedSequence(sequenceNumber);
                      reservation.setReservedAt(LocalDateTime.now());
                      reservation.setStatus(ReservedSequenceRepository.STATUS_PENDING);
                      reservation.setCallerClass(callerClass);
                      reservation.setCallerField(fieldName);

                      JPA.save(reservation);

                      resultRef.set(reservation.getId());
                    } catch (Exception e) {
                      exceptionRef.set(e);
                    }
                  });
          tenantAware.start();
          tenantAware.join();
          return null;
        };

    Future<Void> future = reservationExecutor.submit(callable);

    try {
      future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      log.error("Timeout creating reservation for sequence {}", sequenceId, e);
      throw new RuntimeException("Timeout creating sequence reservation", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted while creating reservation for sequence {}", sequenceId, e);
      throw new RuntimeException("Interrupted while creating sequence reservation", e);
    } catch (ExecutionException e) {
      log.error("Error creating reservation for sequence {}", sequenceId, e);
      throw new RuntimeException("Failed to create sequence reservation", e.getCause());
    }

    // Check if an exception was captured in the nested lambda
    if (exceptionRef.get() != null) {
      log.error(
          "Exception in reservation creation for sequence {}", sequenceId, exceptionRef.get());
      throw new RuntimeException("Failed to create sequence reservation", exceptionRef.get());
    }

    return resultRef.get();
  }

  /**
   * Registers a transaction synchronization callback.
   *
   * <p>This method is made robust to handle cases where no transaction is active or the session is
   * in an invalid state (e.g., during batch imports).
   *
   * @param reservationId the reservation ID
   */
  protected void registerTransactionCallback(Long reservationId) {
    try {
      Session session = JPA.em().unwrap(Session.class);
      if (session == null) {
        log.warn(
            "No Hibernate session available, skipping callback registration for reservation {}",
            reservationId);
        return;
      }

      Transaction transaction = session.getTransaction();
      if (transaction == null) {
        log.warn(
            "No transaction available, skipping callback registration for reservation {}",
            reservationId);
        return;
      }

      if (!transaction.isActive()) {
        log.warn(
            "Transaction is not active, skipping callback registration for reservation {}",
            reservationId);
        return;
      }

      SequenceTransactionSynchronization sync =
          new SequenceTransactionSynchronization(reservationId, this);
      transaction.registerSynchronization(sync);
      log.trace("Registered transaction synchronization for reservation {}", reservationId);

    } catch (Exception e) {
      log.warn(
          "Failed to register transaction callback for reservation {}, reservation will remain PENDING",
          reservationId,
          e);
    }
  }

  @Override
  @PreDestroy
  public void shutdown() {
    log.info("Shutting down SequenceReservationService");
    reservationExecutor.shutdown();
    try {
      if (!reservationExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
        reservationExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      reservationExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Checks if duplicate checking should be performed.
   *
   * @param objectClass the object class
   * @param fieldName the field name
   * @return true if duplicates should be checked
   */
  protected boolean shouldCheckDuplicates(Class<?> objectClass, String fieldName) {
    try {
      return appBaseService.getAppBase().getCheckExistingSequenceOnGeneration()
          && objectClass != null
          && !Strings.isNullOrEmpty(fieldName);
    } catch (Exception e) {
      log.debug("Could not check duplicate configuration", e);
      return false;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks that a sequence number does not already exist.
   */
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void checkSequenceNotExists(
      Class objectClass, String fieldName, String sequenceNumber, Sequence sequence)
      throws AxelorException {

    String table = objectClass.getSimpleName();
    String baseQuery = "SELECT self FROM " + table + " self WHERE " + fieldName + " = :nextSeq";
    String companyQuery = computeCompanyQuery(objectClass);

    TypedQuery<?> query =
        JPA.em()
            .createQuery(baseQuery + companyQuery, objectClass)
            .setParameter("nextSeq", sequenceNumber);

    if (!StringUtils.isEmpty(companyQuery)) {
      query.setParameter("company", sequence.getCompany());
    }

    if (CollectionUtils.isNotEmpty(query.getResultList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_ALREADY_EXISTS),
          sequenceNumber,
          sequence.getFullName());
    }
  }

  /**
   * Computes the company query part for duplicate checking.
   *
   * @param objectClass the entity class
   * @return the query string
   */
  @SuppressWarnings("rawtypes")
  protected String computeCompanyQuery(Class objectClass) {
    Property property =
        Stream.of(Mapper.of(objectClass).getProperties())
            .filter(p -> p.getTarget() == com.axelor.apps.base.db.Company.class)
            .findFirst()
            .orElse(null);

    if (property == null) {
      return "";
    }

    PropertyType type = property.getType();
    String name = property.getName();

    switch (type) {
      case MANY_TO_MANY:
        return " AND :company MEMBER OF self." + name;
      case ONE_TO_MANY:
        return " AND EXISTS (SELECT c FROM self." + name + " c WHERE c = :company)";
      default:
        return " AND self." + name + " = :company";
    }
  }
}
