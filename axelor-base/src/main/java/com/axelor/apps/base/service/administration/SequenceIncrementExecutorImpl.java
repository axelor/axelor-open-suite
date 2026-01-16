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
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.db.tenants.TenantAware;
import com.axelor.i18n.I18n;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link SequenceIncrementExecutor} using a single-thread executor.
 *
 * <p>This implementation ensures that all sequence increment operations are serialized through a
 * single thread, eliminating the need for long-lived database locks while maintaining thread
 * safety.
 */
@Singleton
@ThreadSafe
public class SequenceIncrementExecutorImpl implements SequenceIncrementExecutor {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int DEFAULT_TIMEOUT_SECONDS = 5;

  private final ExecutorService executor;
  private final AppBaseService appBaseService;
  private final SequenceVersionGeneratorService sequenceVersionGeneratorService;

  @Inject
  public SequenceIncrementExecutorImpl(
      AppBaseService appBaseService,
      SequenceVersionGeneratorService sequenceVersionGeneratorService) {
    this.appBaseService = appBaseService;
    this.sequenceVersionGeneratorService = sequenceVersionGeneratorService;
    this.executor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = new Thread(r, "sequence-increment-executor");
              t.setDaemon(true);
              return t;
            });
    log.info("SequenceIncrementExecutor initialized with single-thread executor");
  }

  @Override
  public IncrementResult incrementAndGet(Long sequenceId, LocalDate refDate)
      throws AxelorException {
    int timeoutSeconds = getTimeoutSeconds();

    // Use AtomicReference to capture result from nested lambda
    AtomicReference<IncrementResult> resultRef = new AtomicReference<>();
    AtomicReference<Exception> exceptionRef = new AtomicReference<>();

    Callable<Void> callable =
        () -> {
          TenantAware tenantAware =
              new TenantAware(
                  () -> {
                    try {
                      IncrementResult result = doIncrement(sequenceId, refDate);
                      resultRef.set(result);
                    } catch (Exception e) {
                      exceptionRef.set(e);
                    }
                  });
          tenantAware.start();
          tenantAware.join();
          return null;
        };

    Future<Void> future = executor.submit(callable);

    try {
      future.get(timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_INCREMENT_TIMEOUT),
          sequenceId);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_INCREMENT_INTERRUPTED),
          sequenceId);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof AxelorException) {
        throw (AxelorException) cause;
      }
      throw new AxelorException(
          cause,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_INCREMENT_FAILED),
          sequenceId);
    }

    // Check if an exception was captured in the nested lambda
    if (exceptionRef.get() != null) {
      Exception captured = exceptionRef.get();
      if (captured instanceof AxelorException) {
        throw (AxelorException) captured;
      }
      throw new AxelorException(
          captured,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_INCREMENT_FAILED),
          sequenceId);
    }

    IncrementResult result = resultRef.get();
    if (result == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.SEQUENCE_INCREMENT_FAILED),
          sequenceId);
    }

    log.trace(
        "Sequence {} incremented: versionId={}, nextNum={}",
        sequenceId,
        result.getSequenceVersionId(),
        result.getNextNum());

    return result;
  }

  /**
   * Performs the actual increment operation within a transaction.
   *
   * @param sequenceId the sequence ID
   * @param refDate the reference date
   * @return the increment result
   */
  protected IncrementResult doIncrement(Long sequenceId, LocalDate refDate) {
    // Acquire pessimistic write lock on the sequence with timeout
    Sequence sequence =
        JPA.em()
            .createQuery("SELECT self FROM Sequence self WHERE self.id = :id", Sequence.class)
            .setParameter("id", sequenceId)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setHint("javax.persistence.lock.timeout", 30000)
            .setFlushMode(FlushModeType.COMMIT)
            .getSingleResult();

    // Get or create the sequence version for the reference date
    SequenceVersion sequenceVersion = getOrCreateVersion(sequence, refDate);

    // Get the current nextNum (this is the value we're reserving)
    Long nextNum = sequenceVersion.getNextNum();

    // Increment the counter for the next caller
    sequenceVersion.setNextNum(nextNum + sequence.getToBeAdded());

    // Save the version if it's new
    if (sequenceVersion.getId() == null) {
      JPA.save(sequenceVersion);
    }

    // Flush to ensure the increment is persisted
    JPA.flush();

    return new IncrementResult(sequenceVersion.getId(), nextNum);
  }

  /**
   * Gets or creates a sequence version for the given date.
   *
   * @param sequence the sequence
   * @param refDate the reference date
   * @return the sequence version
   */
  protected SequenceVersion getOrCreateVersion(Sequence sequence, LocalDate refDate) {
    // Query for existing version
    SequenceVersion version =
        JPA
            .em()
            .createQuery(
                "SELECT sv FROM SequenceVersion sv "
                    + "WHERE sv.sequence = :sequence "
                    + "AND sv.startDate <= :date "
                    + "AND (sv.endDate IS NULL OR sv.endDate >= :date) "
                    + "ORDER BY sv.startDate DESC",
                SequenceVersion.class)
            .setParameter("sequence", sequence)
            .setParameter("date", refDate)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst()
            .orElse(null);

    if (version == null) {
      // Create new version
      version = sequenceVersionGeneratorService.createNewSequenceVersion(sequence, refDate);
    }

    return version;
  }

  /**
   * Gets the configured timeout in seconds.
   *
   * @return timeout in seconds
   */
  protected int getTimeoutSeconds() {
    try {
      Integer timeout = appBaseService.getAppBase().getSequenceIncrementTimeout();
      if (timeout != null && timeout > 0) {
        return timeout;
      }
    } catch (Exception e) {
      log.debug("Could not get sequence increment timeout from AppBase, using default", e);
    }
    return DEFAULT_TIMEOUT_SECONDS;
  }

  @Override
  @PreDestroy
  public void shutdown() {
    log.info("Shutting down SequenceIncrementExecutor");
    executor.shutdown();
    try {
      if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
