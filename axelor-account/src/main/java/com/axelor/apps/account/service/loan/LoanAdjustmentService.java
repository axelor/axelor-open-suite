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
package com.axelor.apps.account.service.loan;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface LoanAdjustmentService {

  /** Returns the earliest planned (not booked) installment of the loan, or null if none. */
  LoanLine getNextUnpaidLine(Loan loan);

  /**
   * Recomputes the dependent amounts of a single edited installment (row level), depending on which
   * amount was edited (see LoanLine.EDITED_FIELD_*): editing the interest keeps the installment
   * total and adjusts the capital; editing the capital or the insurance recomputes the total. The
   * remaining debt after is updated and the line is flagged manually modified.
   */
  void computeEditedLine(LoanLine loanLine);

  /**
   * Recomputes the following installments after a manual edit, applying the Quadra rules: editing
   * the interest only regularizes the last installment; editing the capital re-amortizes all the
   * following installments; editing the insurance carries the new insurance to the following
   * installments. Booked installments are never touched. Returns the whole ordered schedule.
   */
  List<LoanLine> recomputeSchedule(Loan loan) throws AxelorException;

  /**
   * Defers the given number of upcoming installments. The loan is extended by that many
   * installments. During the deferral no capital is repaid; the interest is either added to the
   * remaining debt (and the installments optionally recomputed) or paid as interest-only
   * installments; the insurance is optionally kept. A snapshot is taken so the deferral can be
   * cancelled.
   */
  void defer(
      Loan loan,
      int installmentCount,
      boolean capitalizeInterest,
      boolean recomputePayment,
      boolean keepInsurance)
      throws AxelorException;

  /** Restores the planned installments to the state captured before the last deferral. */
  void cancelDeferral(Loan loan) throws AxelorException;
}
