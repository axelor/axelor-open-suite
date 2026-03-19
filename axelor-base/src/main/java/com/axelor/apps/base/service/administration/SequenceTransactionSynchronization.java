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

import com.axelor.db.tenants.TenantAware;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction synchronization callback for sequence reservations.
 *
 * <p>This class is registered with the current transaction to receive callbacks after the
 * transaction completes. Based on the transaction outcome, it either confirms or releases the
 * sequence reservation.
 *
 * <p>The callback executes in a separate transaction to ensure the reservation status update is
 * persisted regardless of the parent transaction's outcome.
 */
public class SequenceTransactionSynchronization implements Synchronization {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Long reservationId;
  private final SequenceReservationService reservationService;

  /**
   * Creates a new transaction synchronization for the given reservation.
   *
   * @param reservationId the ID of the reservation to manage
   * @param reservationService the service to call for confirmation/release
   */
  public SequenceTransactionSynchronization(
      Long reservationId, SequenceReservationService reservationService) {
    this.reservationId = reservationId;
    this.reservationService = reservationService;
  }

  @Override
  public void beforeCompletion() {
    // Nothing to do before completion
  }

  @Override
  public void afterCompletion(int status) {
    log.trace(
        "Transaction completed with status {} for reservation {}",
        statusToString(status),
        reservationId);

    // Validate dependencies before attempting execution
    if (reservationService == null) {
      log.error("ReservationService is null, cannot update reservation {} status", reservationId);
      return;
    }

    if (reservationId == null) {
      log.error("ReservationId is null, cannot update reservation status");
      return;
    }

    // Execute in tenant context with comprehensive error handling
    try {
      TenantAware tenantAware =
          new TenantAware(
              () -> {
                try {
                  if (status == Status.STATUS_COMMITTED) {
                    log.trace("Confirming reservation {}", reservationId);
                    reservationService.confirmReservation(reservationId);
                  } else {
                    // Any other status (ROLLED_BACK, UNKNOWN, etc.) means the transaction
                    // failed - keep as DEBUG since rollback is noteworthy
                    log.debug("Releasing reservation {} due to rollback", reservationId);
                    reservationService.releaseReservation(reservationId);
                  }
                } catch (Exception innerEx) {
                  // Log inner exception but don't rethrow - allow outer catch to handle
                  log.error(
                      "Error in JPA transaction for reservation {} update", reservationId, innerEx);
                }
              });
      tenantAware.start();
      tenantAware.join();
    } catch (Exception e) {
      // Log but don't rethrow - afterCompletion must not throw
      // This reservation will remain in PENDING state and can be cleaned up by batch job
      log.error(
          "Failed to update reservation {} status after transaction completion. "
              + "Reservation will remain in PENDING state.",
          reservationId,
          e);
    }
  }

  /**
   * Converts a transaction status code to a human-readable string.
   *
   * @param status the status code
   * @return the status name
   */
  private static String statusToString(int status) {
    switch (status) {
      case Status.STATUS_COMMITTED:
        return "COMMITTED";
      case Status.STATUS_ROLLEDBACK:
        return "ROLLED_BACK";
      case Status.STATUS_UNKNOWN:
        return "UNKNOWN";
      case Status.STATUS_NO_TRANSACTION:
        return "NO_TRANSACTION";
      case Status.STATUS_ACTIVE:
        return "ACTIVE";
      case Status.STATUS_MARKED_ROLLBACK:
        return "MARKED_ROLLBACK";
      case Status.STATUS_PREPARED:
        return "PREPARED";
      case Status.STATUS_PREPARING:
        return "PREPARING";
      case Status.STATUS_COMMITTING:
        return "COMMITTING";
      case Status.STATUS_ROLLING_BACK:
        return "ROLLING_BACK";
      default:
        return "UNKNOWN_STATUS_" + status;
    }
  }
}
