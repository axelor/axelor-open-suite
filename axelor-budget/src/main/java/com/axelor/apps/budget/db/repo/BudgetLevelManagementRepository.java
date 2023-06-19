package com.axelor.apps.budget.db.repo;

import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.inject.Beans;

public class BudgetLevelManagementRepository extends BudgetLevelRepository {

  @Override
  public BudgetLevel copy(BudgetLevel entity, boolean deep) {
    BudgetLevel copy = super.copy(entity, deep);

    Beans.get(BudgetLevelService.class).resetBudgetLevel(copy);

    return copy;
  }
}
