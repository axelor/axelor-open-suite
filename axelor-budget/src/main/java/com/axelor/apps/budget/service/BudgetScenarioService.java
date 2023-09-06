package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import java.util.Map;
import java.util.Set;

public interface BudgetScenarioService {
  Map<String, Object> buildVariableMap(
      BudgetScenario budgetScenario, Set<BudgetScenarioVariable> variablesList)
      throws AxelorException;
}
