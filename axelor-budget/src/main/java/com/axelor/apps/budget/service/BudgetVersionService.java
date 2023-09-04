package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.google.inject.persist.Transactional;

public interface BudgetVersionService {
  @Transactional(rollbackOn = {RuntimeException.class})
  BudgetVersion createNewVersion(GlobalBudget globalBudget, String name);
}
