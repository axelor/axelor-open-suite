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
package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.BaseBatch;
import com.axelor.apps.base.db.ReservedSequence;
import com.axelor.apps.base.db.repo.ReservedSequenceRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Batch for cleaning up orphaned sequence reservations.
 *
 * <p>This batch processes PENDING reservations that are older than a configurable timeout and marks
 * them as RELEASED, making them available for reuse.
 *
 * <p>It also optionally deletes old CONFIRMED reservations to prevent table bloat.
 */
public class BatchReservedSequenceCleanup extends BatchStrategy {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final ReservedSequenceRepository reservedSequenceRepository;

  protected int releasedCount = 0;
  protected int deletedCount = 0;

  @Inject
  public BatchReservedSequenceCleanup(ReservedSequenceRepository reservedSequenceRepository) {
    this.reservedSequenceRepository = reservedSequenceRepository;
  }

  @Override
  protected void process() {
    BaseBatch baseBatch = batch.getBaseBatch();
    int orphanTimeoutMinutes = getOrphanTimeoutMinutes(baseBatch);
    int retentionDays = getRetentionDays(baseBatch);
    boolean deleteOldConfirmed = baseBatch.getDeleteOldConfirmedReservations();

    log.info(
        "Starting reserved sequence cleanup batch. Orphan timeout: {} minutes, Retention: {} days, Delete old confirmed: {}",
        orphanTimeoutMinutes,
        retentionDays,
        deleteOldConfirmed);

    // Step 1: Release orphaned PENDING reservations
    releaseOrphanedReservations(orphanTimeoutMinutes);

    // Step 2: Optionally delete old CONFIRMED reservations
    if (deleteOldConfirmed && retentionDays > 0) {
      deleteOldConfirmedReservations(retentionDays);
    }
  }

  /**
   * Releases orphaned PENDING reservations older than the timeout.
   *
   * @param timeoutMinutes the timeout in minutes
   */
  protected void releaseOrphanedReservations(int timeoutMinutes) {
    LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

    log.info("Releasing PENDING reservations older than {}", cutoffTime);

    int offset = 0;
    List<ReservedSequence> reservations;

    while (!(reservations = fetchOrphanedReservations(cutoffTime, offset)).isEmpty()) {
      for (ReservedSequence reservation : reservations) {
        try {
          releaseReservation(reservation);
          releasedCount++;
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e, "Reserved Sequence Cleanup - Release", batch.getId());
          incrementAnomaly();
        }
      }
      JPA.clear();
      findBatch();
      offset += getFetchLimit();
    }

    log.info("Released {} orphaned reservations", releasedCount);
  }

  /**
   * Fetches orphaned PENDING reservations.
   *
   * @param cutoffTime the cutoff time
   * @param offset the query offset
   * @return list of orphaned reservations
   */
  protected List<ReservedSequence> fetchOrphanedReservations(LocalDateTime cutoffTime, int offset) {
    return reservedSequenceRepository
        .all()
        .filter("self.status = :status AND self.reservedAt < :cutoffTime")
        .bind("status", ReservedSequenceRepository.STATUS_PENDING)
        .bind("cutoffTime", cutoffTime)
        .order("id")
        .fetch(getFetchLimit(), offset);
  }

  /**
   * Releases a single reservation.
   *
   * @param reservation the reservation to release
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void releaseReservation(ReservedSequence reservation) {
    reservation = reservedSequenceRepository.find(reservation.getId());
    reservation.setStatus(ReservedSequenceRepository.STATUS_RELEASED);
    reservedSequenceRepository.save(reservation);
  }

  /**
   * Deletes old CONFIRMED reservations.
   *
   * @param retentionDays the retention period in days
   */
  protected void deleteOldConfirmedReservations(int retentionDays) {
    LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

    log.info("Deleting CONFIRMED reservations older than {}", cutoffTime);

    int offset = 0;
    List<ReservedSequence> reservations;

    while (!(reservations = fetchOldConfirmedReservations(cutoffTime, offset)).isEmpty()) {
      for (ReservedSequence reservation : reservations) {
        try {
          deleteReservation(reservation);
          deletedCount++;
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e, "Reserved Sequence Cleanup - Delete", batch.getId());
          incrementAnomaly();
        }
      }
      JPA.clear();
      findBatch();
      // No offset increment for delete as records are removed
    }

    log.info("Deleted {} old confirmed reservations", deletedCount);
  }

  /**
   * Fetches old CONFIRMED reservations.
   *
   * @param cutoffTime the cutoff time
   * @param offset the query offset
   * @return list of old confirmed reservations
   */
  protected List<ReservedSequence> fetchOldConfirmedReservations(
      LocalDateTime cutoffTime, int offset) {
    return reservedSequenceRepository
        .all()
        .filter("self.status = :status AND self.reservedAt < :cutoffTime")
        .bind("status", ReservedSequenceRepository.STATUS_CONFIRMED)
        .bind("cutoffTime", cutoffTime)
        .order("id")
        .fetch(getFetchLimit(), offset);
  }

  /**
   * Deletes a single reservation.
   *
   * @param reservation the reservation to delete
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void deleteReservation(ReservedSequence reservation) {
    reservation = reservedSequenceRepository.find(reservation.getId());
    reservedSequenceRepository.remove(reservation);
  }

  /**
   * Gets the orphan timeout in minutes from batch configuration.
   *
   * @param baseBatch the batch configuration
   * @return the timeout in minutes (default 30)
   */
  protected int getOrphanTimeoutMinutes(BaseBatch baseBatch) {
    Integer timeout = baseBatch.getOrphanReservationTimeoutMinutes();
    return timeout != null && timeout > 0 ? timeout : 30;
  }

  /**
   * Gets the retention period in days from batch configuration.
   *
   * @param baseBatch the batch configuration
   * @return the retention in days (default 30)
   */
  protected int getRetentionDays(BaseBatch baseBatch) {
    Integer retention = baseBatch.getConfirmedReservationRetentionDays();
    return retention != null && retention > 0 ? retention : 30;
  }

  @Override
  protected void stop() {
    String comment =
        String.format(
            "Released %d orphaned reservations. Deleted %d old confirmed reservations.",
            releasedCount, deletedCount);
    super.stop();
    addComment(comment);
  }
}
