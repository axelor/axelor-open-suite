package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.VersionExpectedAmountsLine;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class GlobalBudgetWorkflowServiceImpl implements GlobalBudgetWorkflowService {

  protected BudgetLevelService budgetLevelService;

  @Inject
  public GlobalBudgetWorkflowServiceImpl(BudgetLevelService budgetLevelService) {
    this.budgetLevelService = budgetLevelService;
  }

  @Override
  public void validateChildren(GlobalBudget globalBudget, int status) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.validateChildren(budgetLevel);
      }
    }

    globalBudget.setStatusSelect(status);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void archiveChildren(GlobalBudget globalBudget) {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.archiveBudgetLevel(budgetLevel);
      }
    }

    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_ARCHIVED);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void draftChildren(GlobalBudget globalBudget) {

    clearBudgetVersions(globalBudget);

    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.draftChildren(budgetLevel);
      }
    }

    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT);
  }

  protected void clearBudgetVersions(GlobalBudget globalBudget) {
    if (ObjectUtils.isEmpty(globalBudget.getBudgetVersionList())) {
      return;
    }
    globalBudget.setActiveVersion(null);
    for (BudgetVersion budgetVersion : globalBudget.getBudgetVersionList()) {

      List<VersionExpectedAmountsLine> versionExpectedAmountsLines =
          new ArrayList<>(budgetVersion.getVersionExpectedAmountsLineList());
      for (VersionExpectedAmountsLine versionExpectedAmountsLine : versionExpectedAmountsLines) {
        budgetVersion.removeVersionExpectedAmountsLineListItem(versionExpectedAmountsLine);
      }
    }
    List<BudgetVersion> budgetVersionList = new ArrayList<>(globalBudget.getBudgetVersionList());
    for (BudgetVersion budgetversion : budgetVersionList) {
      globalBudget.removeBudgetVersionListItem(budgetversion);
    }

    if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      for (Budget budget : globalBudget.getBudgetList()) {
        budget.setActiveVersionExpectedAmountsLine(null);
      }
    }
  }
}
