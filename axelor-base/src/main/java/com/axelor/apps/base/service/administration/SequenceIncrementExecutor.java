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
import java.time.LocalDate;

/**
 * Service for incrementing sequence numbers in an isolated transaction.
 *
 * <p>This service uses a single-thread executor to serialize all sequence increment operations,
 * ensuring thread-safety without long-lived database locks. Each increment operation executes in
 * its own transaction that commits immediately, releasing the database lock.
 *
 * <p>This approach solves the problem where database locks acquired early in a long transaction
 * (e.g., invoice ventilation) were held until the entire transaction completed, causing blocking
 * issues in high-concurrency environments.
 */
public interface SequenceIncrementExecutor {

  /**
   * Increments the sequence counter in an isolated transaction.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Acquires a PESSIMISTIC_WRITE lock on the Sequence entity
   *   <li>Gets or creates the appropriate SequenceVersion for the reference date
   *   <li>Reads the current nextNum value
   *   <li>Increments nextNum by the sequence's toBeAdded value
   *   <li>Commits the transaction (releasing the lock immediately)
   *   <li>Returns the reserved nextNum value
   * </ol>
   *
   * <p>The entire operation is executed in a separate thread via an ExecutorService, ensuring that:
   *
   * <ul>
   *   <li>All increment operations are serialized (single-thread executor)
   *   <li>The database lock is held only for milliseconds, not for the duration of the calling
   *       transaction
   *   <li>Multi-tenant environments are properly handled via TenantAware
   * </ul>
   *
   * @param sequenceId the ID of the Sequence to increment
   * @param refDate the reference date for version selection (determines which SequenceVersion to
   *     use based on yearly/monthly reset settings)
   * @return an IncrementResult containing the sequence version ID and the reserved nextNum value
   * @throws AxelorException if timeout occurs, thread is interrupted, or increment fails
   */
  IncrementResult incrementAndGet(Long sequenceId, LocalDate refDate) throws AxelorException;

  /**
   * Shuts down the executor service gracefully.
   *
   * <p>This method should be called when the application is shutting down to properly release
   * resources.
   */
  void shutdown();
}
