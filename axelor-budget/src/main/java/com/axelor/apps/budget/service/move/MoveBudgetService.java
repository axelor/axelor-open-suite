/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.AxelorException;

public interface MoveBudgetService {

  /**
   * For each move line : Clear budget distribution, compute the budget key related to this
   * configuration of account and analytic, find the budget related to this key and the move date.
   * Then create an automatic budget distribution with the credit or debit and save the move line.
   * If a budget distribution is not generated, save the move line name in an alert message that
   * will be return.
   *
   * @param move
   * @return String
   */
  public String computeBudgetDistribution(Move move);

  /**
   * For all budgets related to this move, check budget exceed based on global budget control on
   * budget exceed then compute an error message if needed.
   *
   * @param move
   */
  public void getBudgetExceedAlert(Move move) throws AxelorException;

  /**
   * Return if there is budget distribution on any move line
   *
   * @param move
   * @return boolean
   */
  public boolean isBudgetInLines(Move move);

  /**
   * Return if there is no budget distribution on any move line of an accounted or daybooked move
   *
   * @param move
   * @return boolean
   */
  boolean checkMissingBudgetDistributionOnAccountedMove(Move move);
}
