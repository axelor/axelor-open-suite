package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import java.util.ArrayList;
import java.util.List;

public interface BudgetScenarioLineService {
  ArrayList<Integer> getFiscalYears(BudgetScenario budgetScenario);

  void removeUnusedYears(BudgetScenarioLine budgetScenarioLine, int size);

  List<BudgetScenarioLine> getLineUsingSection(
      BudgetLevel section,
      List<BudgetScenarioLine> budgetScenarioLineOriginList,
      List<BudgetScenarioLine> budgetScenarioLineList);
}
