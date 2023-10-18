package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetGenerator;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import java.util.List;

public interface GlobalBudgetService {
  void validateDates(GlobalBudget globalBudget) throws AxelorException;

  void computeBudgetLevelTotals(Budget budget);

  void computeTotals(GlobalBudget globalBudget);

  void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException;

  List<Budget> getAllBudgets(GlobalBudget globalBudget);

  List<Long> getAllBudgetIds(GlobalBudget globalBudget);

  GlobalBudget changeBudgetVersion(GlobalBudget globalBudget, BudgetVersion budgetVersion)
      throws AxelorException;

  GlobalBudget generateGlobalBudget(BudgetGenerator budgetGenerator, Year year)
      throws AxelorException;

  List<BudgetScenarioLine> visualizeVariableAmounts(BudgetGenerator budgetGenerator)
      throws AxelorException;

  void fillGlobalBudgetOnBudget(GlobalBudget globalBudget);

  void updateGlobalBudgetDates(GlobalBudget globalBudget) throws AxelorException;

  /**
   * Return the global budget check available select
   *
   * @param budget
   * @return Integer
   */
  public Integer getBudgetControlLevel(Budget budget);

  List<BudgetLevel> getFilteredBudgetLevelList(GlobalBudget globalBudget);

  List<BudgetLevel> getOtherUsersBudgetLevelList(GlobalBudget globalBudget);
}
