package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;

public interface BudgetVersionService {

  BudgetVersion createNewVersion(GlobalBudget globalBudget, String name);
}
