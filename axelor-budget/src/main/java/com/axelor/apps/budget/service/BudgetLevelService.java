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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface BudgetLevelService {

  /**
   * This function computes all totalAmounts of {@link BudgetLevel} list.
   *
   * @param budgetLevel
   */
  public void computeTotals(BudgetLevel budgetLevel);

  /**
   * This function computes all totalAmounts of {@link Budget} list.
   *
   * @param budgetLevel
   */
  public void computeBudgetTotals(BudgetLevel budgetLevel);

  /**
   * This function creates Global budget (budget) i.e. level 1 {@link BudgetLevel} from Global
   * budget (template)
   *
   * @param budgetLevel
   * @return BudgetLevel
   */
  public BudgetLevel createGlobalBudgets(BudgetLevel budgetLevel);

  /**
   * This function imports and updates BudgetLevel.
   *
   * @param budgetLevel
   * @throws AxelorException
   * @throws ClassNotFoundException
   * @throws IOException
   * @return Error log metaFile
   */
  public MetaFile importBudgetLevel(BudgetLevel budgetLevel)
      throws ClassNotFoundException, AxelorException, IOException;

  /**
   * This function computes totalAmountExpected of the BudgetLevel and from its list.
   *
   * @param budgetLevel
   * @throws AxelorException
   */
  public void computeBudgetLevel(BudgetLevel budgetLevel) throws AxelorException;

  /**
   * Archive the global budget and archive all related budget levels and budgets
   *
   * @param budgetLevel
   * @return BudgetLevel
   */
  public void archiveBudgetLevel(BudgetLevel budgetLevel);

  /**
   * Find the budget level in database then set their dates and save it
   *
   * @param budgetLevel, fromDate, toDate
   * @throws AxelorException
   */
  public void updateBudgetLevelDates(BudgetLevel budgetLevel, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  /**
   * This function set current BudgetLevel to the new project if budget in project is null.
   *
   * @param budgetLevel
   */
  public void setProjectBudget(BudgetLevel budgetLevel);

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
   * Return the global budget check available select
   *
   * @param budget
   * @return Integer
   */
  public Integer getBudgetControlLevel(Budget budget);

  /**
   * Create budget key for each budget related to this section
   *
   * @param section
   * @throws AxelorException
   */
  public void computeChildrenKey(BudgetLevel section) throws AxelorException;

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

  void getUpdatedGroupBudgetLevelList(
      List<BudgetLevel> groupBudgetLevelList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  void getUpdatedSectionBudgetList(
      List<BudgetLevel> sectionBudgetLevelList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  void getUpdatedBudgetList(List<Budget> budgetList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  /**
   * This function computes all totals of {@link BudgetLevel}
   *
   * @param budget
   */
  public void computeBudgetLevelTotals(Budget budget);

  void resetBudgetLevel(BudgetLevel budgetLevel);
}
