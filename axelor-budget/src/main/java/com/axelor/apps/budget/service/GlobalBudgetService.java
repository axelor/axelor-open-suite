package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetGenerator;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import java.util.List;

public interface GlobalBudgetService {
  void validateDates(GlobalBudget globalBudget) throws AxelorException;

  void computeBudgetLevelTotals(Budget budget);

  void computeTotals(GlobalBudget globalBudget);

  void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException;

  GlobalBudget changeBudgetVersion(GlobalBudget globalBudget, BudgetVersion budgetVersion)
      throws AxelorException;

  GlobalBudget generateGlobalBudget(BudgetGenerator budgetGenerator) throws AxelorException;

  List<BudgetScenarioLine> visualizeVariableAmounts(BudgetGenerator budgetGenerator)
      throws AxelorException;
}
