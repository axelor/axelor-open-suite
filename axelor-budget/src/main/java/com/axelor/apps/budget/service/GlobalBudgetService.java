package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.GlobalBudgetTemplate;
import com.google.inject.persist.Transactional;

public interface GlobalBudgetService {
  void validateDates(GlobalBudget globalBudget) throws AxelorException;

  @Transactional(rollbackOn = {RuntimeException.class})
  void computeBudgetLevelTotals(Budget budget);

  @Transactional(rollbackOn = {RuntimeException.class})
  void recomputeGlobalBudgetTotals(GlobalBudget globalBudget);

  void computeTotals(GlobalBudget globalBudget);

  void validateChildren(GlobalBudget globalBudget) throws AxelorException;

  void archiveChildren(GlobalBudget globalBudget) throws AxelorException;

  void draftChildren(GlobalBudget globalBudget) throws AxelorException;

  void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException;

  GlobalBudget generateGlobalBudgetWithTemplate(GlobalBudgetTemplate globalBudgetTemplate)
      throws AxelorException;

  GlobalBudget changeBudgetVersion(GlobalBudget globalBudget, BudgetVersion budgetVersion)
      throws AxelorException;
}
