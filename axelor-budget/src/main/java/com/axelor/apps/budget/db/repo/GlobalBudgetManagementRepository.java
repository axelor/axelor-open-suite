package com.axelor.apps.budget.db.repo;

import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.service.GlobalBudgetResetToolService;
import com.axelor.inject.Beans;

public class GlobalBudgetManagementRepository extends GlobalBudgetRepository {

  @Override
  public GlobalBudget copy(GlobalBudget entity, boolean deep) {
    GlobalBudget copy = super.copy(entity, deep);

    Beans.get(GlobalBudgetResetToolService.class).resetGlobalBudget(copy);

    return copy;
  }
}
