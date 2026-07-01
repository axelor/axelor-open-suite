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

import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;

public interface LoanLineMoveService {

  /**
   * Builds the balanced accounting entry of an installment: debit of the borrowing debt account
   * (capital), the interest expense account (interest) and the insurance expense account
   * (insurance), credit of the bank account (total paid). The move is saved but not booked.
   *
   * @throws AxelorException if the loan journal or one of the required accounts is missing.
   */
  Move generateMove(LoanLine loanLine) throws AxelorException;

  /**
   * Books an installment: generates its accounting entry, posts it through the standard move
   * validation, links the move on the line and updates the loan (Ongoing on the first installment,
   * Closed and remaining debt set to zero on the last one). Installments must be booked in
   * chronological order.
   *
   * @param isBatch true when called from a batch, to relax the per-line previous-line check.
   * @throws AxelorException if the loan is not validated, the installment is already booked or an
   *     earlier installment is still planned.
   */
  Move postInstallment(LoanLine loanLine, boolean isBatch) throws AxelorException;
}
