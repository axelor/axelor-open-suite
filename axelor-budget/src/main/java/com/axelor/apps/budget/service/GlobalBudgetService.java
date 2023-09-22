package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.GlobalBudgetTemplate;

public interface GlobalBudgetService {
  void validateDates(GlobalBudget globalBudget) throws AxelorException;

  void computeBudgetLevelTotals(Budget budget);

  void computeTotals(GlobalBudget globalBudget);

  void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException;

  GlobalBudget generateGlobalBudgetWithTemplate(GlobalBudgetTemplate globalBudgetTemplate)
      throws AxelorException;

  GlobalBudget changeBudgetVersion(GlobalBudget globalBudget, BudgetVersion budgetVersion)
      throws AxelorException;

  void resetGlobalBudget(GlobalBudget globalBudget);
}
