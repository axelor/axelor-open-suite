package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import java.util.List;
import java.util.Map;

public interface GlobalBudgetToolsService {
  List<Budget> getAllBudgets(GlobalBudget globalBudget);

  List<Budget> getAllBudgets(BudgetLevel budgetLevel, List<Budget> budgetList);

  List<Long> getAllBudgetIds(GlobalBudget globalBudget);

  void fillGlobalBudgetOnBudget(GlobalBudget globalBudget);

  Map<String, Map<String, Object>> manageHiddenAmounts(boolean hidden);
}
