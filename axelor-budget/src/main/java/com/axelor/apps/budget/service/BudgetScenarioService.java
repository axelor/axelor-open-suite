package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetScenario;
import java.util.Map;

public interface BudgetScenarioService {
  Map<String, Object> buildVariableMap(BudgetScenario budgetScenario) throws AxelorException;
}
