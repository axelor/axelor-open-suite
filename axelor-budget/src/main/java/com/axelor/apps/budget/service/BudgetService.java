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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.GlobalBudget;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BudgetService {

  /**
   * This function aggregates total amount paid from all {@link BudgetLine}s
   *
   * @param budget
   * @return BigDecimal
   */
  public BigDecimal computeTotalAmountPaid(Budget budget);

  /**
   * This function aggregates total amount committed from all {@link BudgetLine}s
   *
   * @param budget
   * @return BigDecimal
   */
  public BigDecimal computeTotalAmountCommitted(Budget budget);

  /**
   * Set from date and to date of selected budget and save it
   *
   * @param budget, fromDate, toDate
   * @throws AxelorException
   */
  public void updateBudgetDates(Budget budget, LocalDate fromDate, LocalDate toDate);

  /**
   * Compute all firm gap in budget lines and set the total firm gap on budget then save it
   *
   * @param budget
   */
  public void computeTotalFirmGap(Budget budget);

  /**
   * Clear and generate budget lines using budget fields (period duration select, amount and dates)
   *
   * @param budget
   * @return List BudgetLine
   * @throws AxelorException
   */
  public List<BudgetLine> generatePeriods(Budget budget) throws AxelorException;

  /**
   * Compute all to be commited amount in budget lines and return it
   *
   * @param budget
   * @return BigDecimal
   */
  public BigDecimal computeToBeCommittedAmount(Budget budget);

  /**
   * Compute all firm gap amount in budget lines, set the total firm gap on budget and return it
   *
   * @param budget
   * @return BigDecimal
   */
  public BigDecimal computeFirmGap(Budget budget);

  /**
   * Check if budget key are filled in all budgets if needed, validate budget and save it Throw an
   * exception if budget key is missing
   *
   * @param budget, checkBudgetKey
   * @throws AxelorException
   */
  public void validateBudget(Budget budget, boolean checkBudgetKey) throws AxelorException;

  /**
   * Set the budget to draft and save it
   *
   * @param budget
   */
  public void draftBudget(Budget budget);

  /**
   * Compute budget key for this budget and this company, then check if the budget key already
   * exists. If it exists, return an error string
   *
   * @param budget, company
   * @return String
   */
  public String computeBudgetKey(Budget budget, Company company);

  /**
   * Check that account set, analytic distribution list and their percentages and return if there is
   * an error
   *
   * @param budget, company
   * @return String
   */
  public String checkPreconditions(Budget budget, Company company);

  /**
   * Check if the key is already existing in an other budget not archived. Return true if the key is
   * not existing
   *
   * @param budget, key
   * @return boolean
   */
  public boolean checkUniqueKey(Budget budget, String key);

  /**
   * Check that account set and analytic lines are not empty on budget, then copy analytic key for
   * all accounts and return the key
   *
   * @param budget, company
   * @return String
   */
  public String computeKey(Budget budget, Company company);

  /**
   * Generate the key for the account and analytic list and return it. Used with Purchase Order,
   * Invoice and Move process
   *
   * @param account, company, analyticMoveLine
   * @return String
   */
  public String computeKey(Account account, Company company, AnalyticMoveLine analyticMoveLine);

  /**
   * Check if this key exists in a valid budget, on the same dates. Return it if yes
   *
   * @param key, date
   * @return Budget
   */
  public Budget findBudgetWithKey(String key, LocalDate date);

  /**
   * Take the analytic distribution line and concat their axis and account to create the analytic
   * part of the key. Then return it
   *
   * @param analyticAxis analyticAccount
   * @return String
   */
  public String computeAnalyticDistributionLineKey(
      AnalyticAxis analyticAxis, AnalyticAccount analyticAccount);

  /**
   * Take all analytic move line and concat their axis and account to create the analytic part of
   * the key. Then return it
   *
   * @param analyticMoveLine
   * @return String
   */
  public String computeAnalyticMoveLineKey(AnalyticMoveLine analyticMoveLine);

  /**
   * If move not new and not from an invoice, update budgets linked to movelines
   *
   * @param move, excludeMoveInSimulated
   */
  public void updateBudgetLinesFromMove(Move move, boolean excludeMoveInSimulated);

  /**
   * Take all budget distribution and update all amounts
   *
   * @param budgetDistributionList, move, moveLine, excludeMoveInSimulated
   */
  public void updateLinesFromMove(
      List<BudgetDistribution> budgetDistributionList,
      Move move,
      MoveLine moveLine,
      boolean excludeMoveInSimulated);

  /**
   * Find the budget line with move date and update it amounts (realized wih no po, amount realized,
   * to be committed, firm gap, available)
   *
   * @param budgetDistribution, move, moveLine
   */
  public boolean updateLineFromMove(
      BudgetDistribution budgetDistribution, Move move, MoveLine moveLine);

  /**
   * Compute all simulated amount in budget lines, set the total simulated on budget and return it
   *
   * @param move, budget, excludeMoveInSimulated
   * @return BigDecimal
   */
  public BigDecimal computeTotalSimulatedAmount(
      Move move, Budget budget, boolean excludeMoveInSimulated);

  /**
   * Compute all available with simulated amount in budget lines, set the total available with
   * simulated on budget and set on budget
   *
   * @param budget
   */
  public void computeTotalAvailableWithSimulatedAmount(Budget budget);

  /**
   * If budget key is allowed (via config), check that analytic and account are filled then compute
   * key and check if unique. An error can be throwed at every stage of process.
   *
   * @param budget, company
   * @throws AxelorException
   */
  public void createBudgetKey(Budget budget, Company company) throws AxelorException;

  /**
   * Get all accounts that are linked to this company and active from immobilisation, payable,
   * receivable, tax, charge or income type.
   *
   * @param companyId
   * @return String
   * @throws AxelorException
   */
  String getAccountIdList(Long companyId, int budgetType) throws AxelorException;

  /**
   * Return if those budgets are on the same period (fromDate and toDate)
   *
   * @param budget, budgetKey
   * @return boolean
   */
  boolean isInSameDates(Budget budget, Budget budgetKey);

  /**
   * Check if the budget is in section period. Throw an error if no.
   *
   * @param budget
   * @throws AxelorException
   */
  void checkBudgetParentDates(Budget budget) throws AxelorException;

  /**
   * Check if the budget line is in budget period. Throw an error if no. Then throw an error if 2
   * budget lines are on the same period.
   *
   * @param budget
   * @throws AxelorException
   */
  void checkBudgetLinesDates(Budget budget) throws AxelorException;

  /**
   * Check if the budget is in section period. Throw an error if no. Check if the budget line is in
   * budget period. Throw an error if no. Then throw an error if 2 budget lines are on the same
   * period.
   *
   * @param budget
   * @throws AxelorException
   */
  void checkDatesOnBudget(Budget budget) throws AxelorException;

  /**
   * Reset budget amounts and status when the budget is a copy
   *
   * @param entity
   * @return Budget
   */
  void getUpdatedBudgetLineList(Budget budget, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  List<Long> getAnalyticAxisInConfig(Company company) throws AxelorException;

  void validateBudgetDistributionAmounts(
      List<BudgetDistribution> budgetDistributionList, BigDecimal amount, String code)
      throws AxelorException;

  public BigDecimal computeTotalAmount(Budget budget);

  public List<BudgetLine> updateLines(Budget budget);

  public BigDecimal computeTotalAmountRealized(Budget budget);

  void computeAvailableFields(Budget budget);

  void archiveBudget(Budget budget);

  void generateLineFromGenerator(Budget budget, BudgetLevel parent, GlobalBudget globalBudget)
      throws AxelorException;

  void generateLineFromGenerator(
      BudgetScenarioVariable budgetScenarioVariable,
      BudgetLevel parent,
      Map<String, Object> variableAmountMap,
      GlobalBudget globalBudget)
      throws AxelorException;
}
