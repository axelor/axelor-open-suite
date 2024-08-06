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
package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BudgetLevelService {

  /**
   * This function computes all totalAmounts of {@link BudgetLevel} list.
   *
   * @param budgetLevel
   */
  public void computeTotals(BudgetLevel budgetLevel);

  /**
   * Archive the global budget and archive all related budget levels and budgets
   *
   * @param budgetLevel
   * @return BudgetLevel
   */
  public void archiveChildren(BudgetLevel budgetLevel);

  /**
   * Find the budget level in database then set their dates and save it
   *
   * @param budgetLevel, fromDate, toDate
   */
  public void updateBudgetLevelDates(BudgetLevel budgetLevel, LocalDate fromDate, LocalDate toDate);

  /**
   * Set the status to valid then save it
   *
   * @param budgetLevel
   * @throws AxelorException
   */
  public void validateChildren(BudgetLevel budgetLevel) throws AxelorException;

  /**
   * Set the status to draft for the budget level and his children, then related budgets
   *
   * @param budgetLevel
   * @throws AxelorException
   */
  public void draftChildren(BudgetLevel budgetLevel);

  /**
   * Check in all children budget that dates are in the parent period
   *
   * @param global
   * @throws AxelorException
   */
  public void validateDates(BudgetLevel global) throws AxelorException;

  /**
   * Throw an error if the budget level dates are not in
   *
   * @param budgetLevel
   * @throws AxelorException
   */
  public void validateBudgetLevelDates(BudgetLevel budgetLevel) throws AxelorException;

  void getUpdatedBudgetLevelList(
      List<BudgetLevel> budgetLevelList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  void getUpdatedBudgetList(List<Budget> budgetList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  /**
   * This function computes all totals of {@link BudgetLevel}
   *
   * @param budget
   */
  public void computeBudgetLevelTotals(Budget budget);

  List<BudgetLevel> getLastSections(GlobalBudget globalBudget);

  void generateBudgetLevelFromGenerator(
      BudgetLevel budgetLevel,
      BudgetLevel parent,
      GlobalBudget globalBudget,
      Map<String, Object> variableAmountMap,
      boolean linkToGlobal)
      throws AxelorException;
}
