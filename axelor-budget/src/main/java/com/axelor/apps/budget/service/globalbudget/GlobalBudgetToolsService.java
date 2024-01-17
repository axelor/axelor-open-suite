package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import java.util.List;

public interface GlobalBudgetToolsService {
  List<Budget> getAllBudgets(GlobalBudget globalBudget);

  List<Budget> getAllBudgets(BudgetLevel budgetLevel, List<Budget> budgetList);

  List<Long> getAllBudgetIds(GlobalBudget globalBudget);

  List<Long> getAllBudgetLineIds(GlobalBudget globalBudget);

  List<BudgetLevel> getAllBudgetLevels(GlobalBudget globalBudget);

  List<BudgetLevel> getAllBudgetLevels(BudgetLevel budgetLevel, List<BudgetLevel> budgetLevelList);

  List<Long> getAllBudgetLevelIds(GlobalBudget globalBudget);

  void fillGlobalBudgetOnBudget(GlobalBudget globalBudget);
}
