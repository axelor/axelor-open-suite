package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.google.inject.Inject;

public class GlobalBudgetGroupServiceImpl implements GlobalBudgetGroupService {

  protected GlobalBudgetService globalBudgetService;
  protected GlobalBudgetWorkflowService globalBudgetWorkflowService;

  @Inject
  public GlobalBudgetGroupServiceImpl(
      GlobalBudgetService globalBudgetService,
      GlobalBudgetWorkflowService globalBudgetWorkflowService) {
    this.globalBudgetService = globalBudgetService;
    this.globalBudgetWorkflowService = globalBudgetWorkflowService;
  }

  @Override
  public void validateStructure(GlobalBudget globalBudget) throws AxelorException {
    globalBudgetService.generateBudgetKey(globalBudget);

    globalBudgetWorkflowService.validateChildren(
        globalBudget, GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID_STRUCTURE);
  }
}
