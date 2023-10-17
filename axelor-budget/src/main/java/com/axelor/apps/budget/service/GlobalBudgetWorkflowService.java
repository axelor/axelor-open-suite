package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.GlobalBudget;

public interface GlobalBudgetWorkflowService {

  void validateChildren(GlobalBudget globalBudget, int status) throws AxelorException;

  void validateStructure(GlobalBudget globalBudget) throws AxelorException;

  void archiveChildren(GlobalBudget globalBudget);

  void draftChildren(GlobalBudget globalBudget);
}
