package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.GlobalBudget;

public interface GlobalBudgetWorkflowService {

  void validateChildren(GlobalBudget globalBudget, int status) throws AxelorException;

  void archiveChildren(GlobalBudget globalBudget) throws AxelorException;

  void draftChildren(GlobalBudget globalBudget) throws AxelorException;
}
