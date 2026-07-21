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
import java.math.BigDecimal;

public interface LoanConsistencyService {

  /** Theoretical outstanding capital: sum of the capital of the not-yet-booked installments. */
  BigDecimal computeTheoreticalOutstanding(Loan loan);

  /** Accounting (credit) balance of the loan borrowing debt account (164). */
  BigDecimal computeAccountBalance(Loan loan);

  /** Gap between the theoretical outstanding capital and the account 164 balance. */
  BigDecimal computeGap(Loan loan);
}
