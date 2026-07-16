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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanLineComputationService {

  /** Computes the amortization schedule lines for the loan (not attached/persisted). */
  List<LoanLine> computeLines(Loan loan) throws AxelorException;

  /**
   * Computes {@code count} amortization lines starting from a given remaining debt and date, using
   * the loan computation mode and rate. Used to regenerate the tail of the schedule after a manual
   * adjustment or a deferral (analogous to FixedAsset
   * generateAndComputeFixedAssetLinesStartingWith). Lines are not attached/persisted.
   */
  List<LoanLine> computeLinesFrom(
      Loan loan, BigDecimal startRemainingDebt, LocalDate firstDate, int count)
      throws AxelorException;
}
