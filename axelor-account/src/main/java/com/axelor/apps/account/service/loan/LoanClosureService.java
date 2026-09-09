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
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface LoanClosureService {

  /**
   * Accrued interest not yet due (ICNE) at the closing date: remaining debt after the last
   * installment due on or before the closing date, times the annual rate, prorated over the number
   * of days elapsed since that installment on a 360-day basis.
   */
  BigDecimal computeAccruedInterest(Loan loan, LocalDate closingDate);

  /**
   * Prepaid insurance (CCA) at the closing date: the share of the insurance of the installment
   * straddling the closing date that covers the days after the closing date, prorated over the
   * installment period.
   */
  BigDecimal computePrepaidInsurance(Loan loan, LocalDate closingDate);

  /**
   * Builds the year-end adjustment move for a loan at the closing date: debit interest expense /
   * credit accrued interest for the accrued interest, and debit prepaid expense / credit insurance
   * expense for the prepaid insurance. Returns null when both amounts are zero. The move is saved
   * but not booked.
   *
   * @throws AxelorException if the loan journal or one of the required accounts is missing.
   */
  Move generateClosureMove(Loan loan, LocalDate closingDate) throws AxelorException;

  /**
   * Generates the closing adjustment move, books it, and generates its reversal on the day after
   * the closing date. Returns null when there is nothing to adjust.
   *
   * @throws AxelorException if adjustments already exist for this loan and date, or if a required
   *     account is missing.
   */
  Move postClosure(Loan loan, LocalDate closingDate) throws AxelorException;
}
