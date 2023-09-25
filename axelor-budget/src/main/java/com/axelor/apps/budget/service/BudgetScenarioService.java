package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetScenario;
import java.util.Map;

public interface BudgetScenarioService {
  Map<String, Object> buildVariableMap(BudgetScenario budgetScenario, int yearNumber)
      throws AxelorException;

  Map<String, Object> getVariableMap(BudgetScenario budgetScenario, int yearNumber)
      throws AxelorException;

  void validateScenario(BudgetScenario budgetScenario) throws AxelorException;

  void draftScenario(BudgetScenario budgetScenario);
}
