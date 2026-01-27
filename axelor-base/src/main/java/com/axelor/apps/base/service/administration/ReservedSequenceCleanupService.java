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

/**
 * Service for cleaning up orphaned sequence reservations.
 *
 * <p>Orphaned reservations can occur when:
 *
 * <ul>
 *   <li>The JVM crashes after creating a PENDING reservation but before the transaction callback
 *   <li>The transaction callback fails to update the reservation status
 *   <li>Network issues prevent the callback from completing
 * </ul>
 *
 * <p>This service provides methods to identify and release orphaned reservations, making those
 * sequence numbers available for reuse.
 */
public interface ReservedSequenceCleanupService {

  /** Default timeout in minutes for considering a PENDING reservation as orphaned. */
  int DEFAULT_ORPHAN_TIMEOUT_MINUTES = 30;

  /**
   * Cleans up orphaned PENDING reservations.
   *
   * <p>Reservations in PENDING status that are older than the default timeout (30 minutes) are
   * considered orphaned and will be released (status changed to RELEASED).
   *
   * @return the number of reservations that were released
   */
  int cleanupOrphanedReservations();

  /**
   * Cleans up orphaned PENDING reservations with a custom timeout.
   *
   * @param timeoutMinutes the timeout in minutes after which a PENDING reservation is considered
   *     orphaned
   * @return the number of reservations that were released
   */
  int cleanupOrphanedReservations(int timeoutMinutes);

  /**
   * Counts the number of PENDING reservations that are considered orphaned.
   *
   * @param timeoutMinutes the timeout in minutes
   * @return the count of orphaned reservations
   */
  long countOrphanedReservations(int timeoutMinutes);

  /**
   * Deletes old CONFIRMED reservations to prevent table bloat.
   *
   * <p>CONFIRMED reservations are kept for auditing purposes but can be deleted after a retention
   * period.
   *
   * @param retentionDays the number of days to retain CONFIRMED reservations
   * @return the number of reservations that were deleted
   */
  int deleteOldConfirmedReservations(int retentionDays);
}
