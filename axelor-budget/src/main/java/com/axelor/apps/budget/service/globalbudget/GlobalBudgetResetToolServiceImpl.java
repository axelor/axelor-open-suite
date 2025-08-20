/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.BudgetLevelResetToolService;
import com.axelor.apps.budget.service.BudgetResetToolService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GlobalBudgetResetToolServiceImpl implements GlobalBudgetResetToolService {

  protected BudgetLevelResetToolService budgetLevelResetToolService;
  protected BudgetResetToolService budgetResetToolService;
  protected CurrencyScaleService currencyScaleService;
  protected GlobalBudgetToolsService globalBudgetToolsService;

  @Inject
  public GlobalBudgetResetToolServiceImpl(
      BudgetLevelResetToolService budgetLevelResetToolService,
      CurrencyScaleService currencyScaleService,
      BudgetResetToolService budgetResetToolService,
      GlobalBudgetToolsService globalBudgetToolsService) {
    this.budgetLevelResetToolService = budgetLevelResetToolService;
    this.currencyScaleService = currencyScaleService;
    this.budgetResetToolService = budgetResetToolService;
    this.globalBudgetToolsService = globalBudgetToolsService;
  }

  public void resetGlobalBudget(GlobalBudget globalBudget) {
    globalBudget.setCode(globalBudget.getCode() + " (" + I18n.get("copy") + ")");
    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT);
    globalBudget.setArchived(false);

    globalBudget.setTotalAmountCommitted(BigDecimal.ZERO);
    globalBudget.setTotalAmountAvailable(
        currencyScaleService.getCompanyScaledValue(
            globalBudget, globalBudget.getTotalAmountExpected()));
    globalBudget.setAvailableAmountWithSimulated(
        currencyScaleService.getCompanyScaledValue(
            globalBudget, globalBudget.getTotalAmountExpected()));
    globalBudget.setRealizedWithNoPo(BigDecimal.ZERO);
    globalBudget.setRealizedWithPo(BigDecimal.ZERO);
    globalBudget.setSimulatedAmount(BigDecimal.ZERO);
    globalBudget.setTotalFirmGap(BigDecimal.ZERO);
    globalBudget.setTotalAmountPaid(BigDecimal.ZERO);
    globalBudget.setActiveVersion(null);
    globalBudget.clearBudgetVersionList();
    List<BudgetLevel> budgetLevelList = globalBudget.getBudgetLevelList();
    List<Budget> budgetList = globalBudget.getBudgetList();

    if (!ObjectUtils.isEmpty(budgetLevelList)) {
      globalBudget.clearBudgetList();
      budgetLevelList.forEach(budgetLevelResetToolService::resetBudgetLevel);
    } else if (ObjectUtils.isEmpty(budgetList)) {
      budgetList.forEach(budgetResetToolService::resetBudget);
    }
  }

  @Override
  public void clearBudgetList(GlobalBudget globalBudget) {
    if (globalBudget == null) {
      return;
    }

    List<BudgetLevel> budgetLevelList = globalBudgetToolsService.getAllBudgetLevels(globalBudget);
    if (ObjectUtils.isEmpty(budgetLevelList)) {
      globalBudget.clearBudgetList();
    } else if (ObjectUtils.notEmpty(globalBudget.getBudgetList())) {
      List<Budget> budgetToRemoveList = new ArrayList<>();
      for (Budget budget : globalBudget.getBudgetList()) {
        if (!budgetLevelList.contains(budget.getBudgetLevel())) {
          budgetToRemoveList.add(budget);
        }
      }

      if (ObjectUtils.notEmpty(budgetToRemoveList)) {
        for (Budget budget : budgetToRemoveList) {
          globalBudget.removeBudgetListItem(budget);
        }
      }
    }
  }
}
