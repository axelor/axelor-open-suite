package com.axelor.apps.budget.service.advanced.imports;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;

public interface AdvancedImportBudgetService {
  /**
   * Select in the budget level the level and set the root parent budget code
   *
   * @param budgetLevel
   * @return BudgetLevel
   */
  public BudgetLevel setLevelTypeSelect(BudgetLevel budgetLevel);
  /**
   * Set the budget level using his code
   *
   * @param budget
   * @return Budget
   */
  public Budget setBudgetLevel(Budget budget);
}
