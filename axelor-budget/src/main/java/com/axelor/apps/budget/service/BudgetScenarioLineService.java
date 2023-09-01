package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetScenario;
import java.util.*;

public interface BudgetScenarioLineService {
  ArrayList<Integer> getFiscalYears(BudgetScenario budgetScenario);

  boolean countBudgetScenarioLines(BudgetScenario budgetScenario);
}
