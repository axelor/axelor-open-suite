package com.axelor.apps.budget.db.repo;

import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.service.GlobalBudgetService;
import com.axelor.inject.Beans;

public class GlobalBudgetManagementRepository extends GlobalBudgetRepository {

  @Override
  public GlobalBudget save(GlobalBudget entity) {

    GlobalBudgetService globalBudgetService = Beans.get(GlobalBudgetService.class);

    entity = super.save(entity);

    globalBudgetService.fillGlobalBudgetOnBudget(entity);

    return entity;
  }
}
