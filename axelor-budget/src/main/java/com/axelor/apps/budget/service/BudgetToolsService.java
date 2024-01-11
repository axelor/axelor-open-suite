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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.auth.db.User;
import com.axelor.db.Model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BudgetToolsService {

  /**
   * Return if the budget key is enabled in config and user is in a group that have permission to
   * deal with budget keys
   *
   * @param company, user
   * @return boolean
   */
  boolean checkBudgetKeyAndRole(Company company, User user) throws AxelorException;

  boolean checkBudgetKeyAndRoleForMove(Move move) throws AxelorException;

  GlobalBudget getGlobalBudgetUsingBudget(Budget budget);

  GlobalBudget getGlobalBudgetUsingBudgetLevel(BudgetLevel budgetLevel);

  String getBudgetExceedMessage(String budgetExceedAlert, boolean isOrder, boolean isError);

  boolean canAutoComputeBudgetDistribution(Company company, List<? extends Model> list)
      throws AxelorException;

  List<AnalyticAxis> getAuthorizedAnalyticAxis(Company company) throws AxelorException;

  /**
   * Check if budget key are enabled in account config of company
   *
   * @param company
   * @return boolean
   * @throws AxelorException
   */
  public boolean checkBudgetKeyInConfig(Company company) throws AxelorException;

  BigDecimal getAvailableAmountOnBudget(Budget budget, LocalDate date);

  /**
   * Return the global budget check available select
   *
   * @param budget
   * @return Integer
   */
  public Integer getBudgetControlLevel(Budget budget);

  Map<String, BigDecimal> buildMapWithAmounts(
      List<Budget> budgetList, List<BudgetLevel> budgetLevelList);
}
