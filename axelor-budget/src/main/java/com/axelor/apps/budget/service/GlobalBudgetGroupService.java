package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.GlobalBudget;

public interface GlobalBudgetGroupService {
  void validateStructure(GlobalBudget globalBudget) throws AxelorException;
}
