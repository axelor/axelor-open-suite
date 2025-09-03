/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;

public interface MoveLineBudgetService {

  /**
   * Clear budget distribution, compute the budget key related to this configuration of account and
   * analytic, find the budget related to this key and the move date. Then create an automatic
   * budget distribution with the credit or debit and save the move line. Return an alert message if
   * a budget distribution is not generated
   *
   * @param move
   * @param moveLine
   * @return String
   */
  public String computeBudgetDistribution(Move move, MoveLine moveLine) throws AxelorException;

  /**
   * Take all budget distribution and throw an error if the total amount of budget distribution is
   * superior to the debit or credit of the move line
   *
   * @param moveLine
   * @throws AxelorException
   */
  public void checkAmountForMoveLine(MoveLine moveLine) throws AxelorException;

  String getBudgetDomain(Move move, MoveLine moveLine) throws AxelorException;

  void manageMonoBudget(Move move);

  void negateAmount(MoveLine moveLine, Move move);

  void changeBudgetDistribution(MoveLine moveLine);
}
