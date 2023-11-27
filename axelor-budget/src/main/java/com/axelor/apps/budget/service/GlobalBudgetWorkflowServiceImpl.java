/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
  protected BudgetService budgetService;
  protected BudgetToolsService budgetToolsService;
  protected GlobalBudgetService globalBudgetService;

  @Inject
  public GlobalBudgetWorkflowServiceImpl(
      BudgetLevelService budgetLevelService,
      BudgetService budgetService,
      BudgetToolsService budgetToolsService,
      GlobalBudgetService globalBudgetService) {
    this.budgetLevelService = budgetLevelService;
    this.budgetService = budgetService;
    this.budgetToolsService = budgetToolsService;
    this.globalBudgetService = globalBudgetService;
  }

  @Override
  public void validateChildren(GlobalBudget globalBudget, int status) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.validateChildren(budgetLevel);
      }
    } else if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      boolean checkBudgetKey = budgetToolsService.checkBudgetKeyInConfig(globalBudget.getCompany());
      for (Budget budget : globalBudget.getBudgetList()) {
        budgetService.validateBudget(budget, checkBudgetKey);
      }
    }

    globalBudget.setStatusSelect(status);
  }

  @Override
  public void validateStructure(GlobalBudget globalBudget) throws AxelorException {
    globalBudgetService.generateBudgetKey(globalBudget);

    globalBudget.setStatusSelect(
        GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID_STRUCTURE);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void archiveChildren(GlobalBudget globalBudget) {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.archiveBudgetLevel(budgetLevel);
      }
    } else if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      for (Budget budget : globalBudget.getBudgetList()) {
        budgetService.archiveBudget(budget);
      }
    }

    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_ARCHIVED);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void draftChildren(GlobalBudget globalBudget) {

    // clearBudgetVersions(globalBudget);

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
