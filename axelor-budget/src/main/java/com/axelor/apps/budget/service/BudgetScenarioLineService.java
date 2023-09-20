package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import java.util.ArrayList;

public interface BudgetScenarioLineService {
  ArrayList<Integer> getFiscalYears(BudgetScenario budgetScenario);

  void removeUnusedYears(BudgetScenarioLine budgetScenarioLine, int size);
}
