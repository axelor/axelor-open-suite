package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class GlobalBudgetWorkflowServiceImpl implements GlobalBudgetWorkflowService {

  protected BudgetLevelService budgetLevelService;
  protected BudgetService budgetService;
  protected BudgetRepository budgetRepository;

  @Inject
  public GlobalBudgetWorkflowServiceImpl(
      BudgetLevelService budgetLevelService,
      BudgetService budgetService,
      BudgetRepository budgetRepository) {
    this.budgetLevelService = budgetLevelService;
    this.budgetService = budgetService;
    this.budgetRepository = budgetRepository;
  }

  @Override
  public void validateChildren(GlobalBudget globalBudget, int status) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.validateChildren(budgetLevel);
      }
    } else if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      boolean checkBudgetKey = budgetService.checkBudgetKeyInConfig(globalBudget.getCompany());
      for (Budget budget : globalBudget.getBudgetList()) {
        budgetService.validateBudget(budget, checkBudgetKey);
      }
    }

    globalBudget.setStatusSelect(status);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void archiveChildren(GlobalBudget globalBudget) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.archiveBudgetLevel(budgetLevel);
      }
    } else if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      for (Budget budget : globalBudget.getBudgetList()) {
        budget.setArchived(true);
        budgetRepository.save(budget);
      }
    }

    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_ARCHIVED);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void draftChildren(GlobalBudget globalBudget) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.draftChildren(budgetLevel);
      }
    } else if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      for (Budget budget : globalBudget.getBudgetList()) {
        budgetService.draftBudget(budget);
      }
    }

    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT);
  }
}
