/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankstatementquery;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.base.AxelorException;

public interface BankStatementQueryService {

  /**
   * Evaluate the bankstatementQuery and return the resulting object. Only works with
   * bankStatementQuery of type Partner's fetching (2) and Move line's fetching (3). It can not be
   * used with type Accounting auto (0) or Reconciliation auto (1).
   *
   * @param bankStatementQuery: can not be null
   * @param bankStatementLine : will be used as context, can not be null
   * @param move: will be usable in context, can be null if type 2
   * @return the generated object (either a MoveLine or Partner)
   * @throws AxelorException
   */
  Object evalQuery(
      BankStatementQuery bankStatementQuery, BankStatementLine bankStatementLine, Move move)
      throws AxelorException;
}
