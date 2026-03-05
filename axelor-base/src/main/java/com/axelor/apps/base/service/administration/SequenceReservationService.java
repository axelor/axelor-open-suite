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
import com.axelor.db.Model;
import java.time.LocalDate;

/**
 * Service for managing sequence number reservations with transaction-aware lifecycle.
 *
 * <p>This service orchestrates sequence number generation with the following guarantees:
 *
 * <ul>
 *   <li>Numbers are reserved immediately but only confirmed on transaction commit
 *   <li>On transaction rollback, reserved numbers are released for reuse
 *   <li>Released numbers are recycled to minimize gaps in the sequence
 *   <li>Database locks are held only for milliseconds during increment
 * </ul>
 *
 * <p>The reservation lifecycle:
 *
 * <ol>
 *   <li>PENDING: Number reserved, transaction in progress
 *   <li>CONFIRMED: Transaction committed, number permanently assigned
 *   <li>RELEASED: Transaction rolled back, number available for reuse
 * </ol>
 */
public interface SequenceReservationService {

  /**
   * Reserves a sequence number for the current transaction.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>First checks for any RELEASED sequences that can be reused
   *   <li>If none available, increments the sequence via {@link SequenceIncrementExecutor}
   *   <li>Computes the formatted sequence number
   *   <li>Optionally checks for duplicates if configured
   *   <li>Creates a ReservedSequence record with PENDING status
   *   <li>Registers a transaction synchronization callback for commit/rollback handling
   * </ol>
   *
   * @param sequence the sequence configuration
   * @param refDate the reference date for version selection and date patterns
   * @param objectClass the entity class (for duplicate checking), can be null
   * @param fieldName the field name (for duplicate checking), can be null
   * @param model the model instance for Groovy prefix/suffix evaluation, can be null
   * @return the reserved sequence number string
   * @throws AxelorException if reservation fails (timeout, duplicate, etc.)
   */
  String reserveSequenceNumber(
      Sequence sequence, LocalDate refDate, Class<?> objectClass, String fieldName, Model model)
      throws AxelorException;

  /**
   * Confirms a reservation after successful transaction commit.
   *
   * <p>Changes the reservation status from PENDING to CONFIRMED. This method is called
   * automatically by the transaction synchronization callback.
   *
   * @param reservationId the ID of the reservation to confirm
   */
  void confirmReservation(Long reservationId);

  /**
   * Releases a reservation after transaction rollback.
   *
   * <p>Changes the reservation status from PENDING to RELEASED. The released sequence number
   * becomes available for reuse by subsequent calls to {@link #reserveSequenceNumber}. This method
   * is called automatically by the transaction synchronization callback.
   *
   * @param reservationId the ID of the reservation to release
   */
  void releaseReservation(Long reservationId);

  /**
   * Checks that a sequence number does not already exist for the given entity and field.
   *
   * <p>This method verifies uniqueness of a sequence number within the scope of a company (if the
   * entity has a company field).
   *
   * @param objectClass the entity class to check
   * @param fieldName the field name containing the sequence number
   * @param sequenceNumber the sequence number to check
   * @param sequence the sequence configuration (for company scope)
   * @throws AxelorException if the sequence number already exists
   */
  void checkSequenceNotExists(
      Class<?> objectClass, String fieldName, String sequenceNumber, Sequence sequence)
      throws AxelorException;

  /**
   * Shuts down the reservation executor service gracefully.
   *
   * <p>This method should be called when the application is shutting down to properly release
   * resources.
   */
  void shutdown();
}
