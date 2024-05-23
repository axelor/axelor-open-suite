/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.budget.service.BudgetLevelComputeService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class GlobalBudgetComputeServiceImpl implements GlobalBudgetComputeService {

  protected BudgetToolsService budgetToolsService;
  protected BudgetLevelComputeService budgetLevelComputeService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public GlobalBudgetComputeServiceImpl(
      BudgetToolsService budgetToolsService,
      BudgetLevelComputeService budgetLevelComputeService,
      CurrencyScaleService currencyScaleService) {
    this.budgetToolsService = budgetToolsService;
    this.budgetLevelComputeService = budgetLevelComputeService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void computeBudgetLevelTotals(Budget budget) {
    Beans.get(BudgetService.class).computeAvailableFields(budget);
    budgetLevelComputeService.computeBudgetLevelTotals(budget);
    GlobalBudget globalBudget = budget.getGlobalBudget();
    if (globalBudget == null) {
      globalBudget = budgetToolsService.getGlobalBudgetUsingBudget(budget);
    }
    if (globalBudget != null) {
      computeTotals(globalBudget);
    }
  }

  @Override
  public void computeTotals(GlobalBudget globalBudget) {
    List<BudgetLevel> budgetLevelList = globalBudget.getBudgetLevelList();
    List<Budget> budgetList = globalBudget.getBudgetList();
    Map<String, BigDecimal> amountByField =
        budgetToolsService.buildMapWithAmounts(budgetList, budgetLevelList);
    globalBudget.setTotalAmountExpected(amountByField.get("totalAmountExpected"));
    globalBudget.setTotalAmountCommitted(amountByField.get("totalAmountCommitted"));
    globalBudget.setTotalAmountPaid(amountByField.get("totalAmountPaid"));
    globalBudget.setTotalAmountRealized(amountByField.get("totalAmountRealized"));
    globalBudget.setRealizedWithNoPo(amountByField.get("realizedWithNoPo"));
    globalBudget.setRealizedWithPo(amountByField.get("realizedWithPo"));
    globalBudget.setTotalAmountAvailable(
        currencyScaleService.getCompanyScaledValue(
            globalBudget,
            (amountByField
                    .get("totalAmountExpected")
                    .subtract(amountByField.get("realizedWithPo"))
                    .subtract(amountByField.get("realizedWithNoPo")))
                .max(BigDecimal.ZERO)));
    globalBudget.setTotalFirmGap(amountByField.get("totalFirmGap"));
    globalBudget.setSimulatedAmount(amountByField.get("simulatedAmount"));
    globalBudget.setAvailableAmountWithSimulated(
        currencyScaleService.getCompanyScaledValue(
            globalBudget,
            (globalBudget.getTotalAmountAvailable().subtract(amountByField.get("simulatedAmount")))
                .max(BigDecimal.ZERO)));
  }
}
