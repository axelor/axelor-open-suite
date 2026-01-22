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

import com.axelor.apps.base.db.repo.ReservedSequenceRepository;
import com.axelor.db.JPA;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ReservedSequenceCleanupService}.
 *
 * <p>Provides cleanup operations for orphaned sequence reservations.
 */
@Singleton
public class ReservedSequenceCleanupServiceImpl implements ReservedSequenceCleanupService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public int cleanupOrphanedReservations() {
    return cleanupOrphanedReservations(DEFAULT_ORPHAN_TIMEOUT_MINUTES);
  }

  @Override
  public int cleanupOrphanedReservations(int timeoutMinutes) {
    LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

    log.info(
        "Cleaning up orphaned PENDING reservations older than {} minutes (cutoff: {})",
        timeoutMinutes,
        cutoffTime);

    int updatedCount =
        JPA.em()
            .createQuery(
                "UPDATE ReservedSequence rs "
                    + "SET rs.status = :releasedStatus "
                    + "WHERE rs.status = :pendingStatus "
                    + "AND rs.reservedAt < :cutoffTime")
            .setParameter("releasedStatus", ReservedSequenceRepository.STATUS_RELEASED)
            .setParameter("pendingStatus", ReservedSequenceRepository.STATUS_PENDING)
            .setParameter("cutoffTime", cutoffTime)
            .executeUpdate();

    if (updatedCount > 0) {
      log.info("Released {} orphaned reservations", updatedCount);
    } else {
      log.debug("No orphaned reservations found");
    }

    return updatedCount;
  }

  @Override
  public long countOrphanedReservations(int timeoutMinutes) {
    LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

    return JPA.em()
        .createQuery(
            "SELECT COUNT(rs) FROM ReservedSequence rs "
                + "WHERE rs.status = :pendingStatus "
                + "AND rs.reservedAt < :cutoffTime",
            Long.class)
        .setParameter("pendingStatus", ReservedSequenceRepository.STATUS_PENDING)
        .setParameter("cutoffTime", cutoffTime)
        .getSingleResult();
  }

  @Override
  public int deleteOldConfirmedReservations(int retentionDays) {
    LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

    log.info(
        "Deleting CONFIRMED reservations older than {} days (cutoff: {})",
        retentionDays,
        cutoffTime);

    int deletedCount =
        JPA.em()
            .createQuery(
                "DELETE FROM ReservedSequence rs "
                    + "WHERE rs.status = :confirmedStatus "
                    + "AND rs.reservedAt < :cutoffTime")
            .setParameter("confirmedStatus", ReservedSequenceRepository.STATUS_CONFIRMED)
            .setParameter("cutoffTime", cutoffTime)
            .executeUpdate();

    if (deletedCount > 0) {
      log.info("Deleted {} old confirmed reservations", deletedCount);
    } else {
      log.debug("No old confirmed reservations found");
    }

    return deletedCount;
  }
}
