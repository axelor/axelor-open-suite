package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import java.util.Map;

public interface BudgetGroupService {
  Map<String, Object> getOnNewValuesMap(
      Budget budget, BudgetLevel parent, GlobalBudget global, String typeSelect);
}
