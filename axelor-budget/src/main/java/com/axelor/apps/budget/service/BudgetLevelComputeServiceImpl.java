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
package com.axelor.apps.budget.service;

import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Map;

public class BudgetLevelComputeServiceImpl implements BudgetLevelComputeService {

  protected BudgetToolsService budgetToolsService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BudgetLevelComputeServiceImpl(
      BudgetToolsService budgetToolsService, CurrencyScaleService currencyScaleService) {
    this.budgetToolsService = budgetToolsService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void computeTotals(BudgetLevel budgetLevel) {
    Map<String, BigDecimal> amountByField =
        budgetToolsService.buildMapWithAmounts(
            budgetLevel.getBudgetList(), budgetLevel.getBudgetLevelList());
    budgetLevel.setTotalAmountExpected(amountByField.get("totalAmountExpected"));
    budgetLevel.setTotalAmountCommitted(amountByField.get("totalAmountCommitted"));
    budgetLevel.setTotalAmountPaid(amountByField.get("totalAmountPaid"));
    budgetLevel.setTotalAmountRealized(amountByField.get("totalAmountRealized"));
    budgetLevel.setRealizedWithNoPo(amountByField.get("realizedWithNoPo"));
    budgetLevel.setRealizedWithPo(amountByField.get("realizedWithPo"));
    budgetLevel.setTotalAmountAvailable(
        currencyScaleService.getCompanyScaledValue(
            budgetLevel,
            amountByField
                .get("totalAmountExpected")
                .subtract(amountByField.get("realizedWithPo"))
                .subtract(amountByField.get("realizedWithNoPo"))
                .max(BigDecimal.ZERO)));
    budgetLevel.setTotalFirmGap(amountByField.get("totalFirmGap"));
    budgetLevel.setSimulatedAmount(amountByField.get("simulatedAmount"));
    budgetLevel.setAvailableAmountWithSimulated(
        currencyScaleService.getCompanyScaledValue(
            budgetLevel,
            budgetLevel
                .getTotalAmountAvailable()
                .subtract(amountByField.get("simulatedAmount"))
                .max(BigDecimal.ZERO)));
  }

  @Override
  public void computeBudgetLevelTotals(Budget budget) {
    BudgetLevel sectionBudgetLevel = budget.getBudgetLevel();
    computeLevelTotals(sectionBudgetLevel);
  }

  @Transactional
  protected void computeLevelTotals(BudgetLevel budgetLevel) {
    if (budgetLevel == null) {
      return;
    }
    computeTotals(budgetLevel);
    computeLevelTotals(budgetLevel.getParentBudgetLevel());
  }
}
