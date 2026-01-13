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
package com.axelor.apps.budget.service.compute;

import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.service.BudgetToolsService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class BudgetComputeServiceImpl implements BudgetComputeService {

  protected BudgetToolsService budgetToolsService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BudgetComputeServiceImpl(
      BudgetToolsService budgetToolsService, CurrencyScaleService currencyScaleService) {
    this.budgetToolsService = budgetToolsService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void computeBudgetFieldsWithLines(Budget budget) {
    if (budget == null) {
      return;
    }

    Map<String, BigDecimal> amountByField = budgetToolsService.computeBudgetAmountMap(budget);

    budget.setTotalAmountExpected(amountByField.get("totalAmountExpected"));
    budget.setTotalAmountCommitted(amountByField.get("totalAmountCommitted"));
    budget.setTotalAmountPaid(amountByField.get("totalAmountPaid"));
    budget.setTotalAmountRealized(amountByField.get("totalAmountRealized"));
    budget.setRealizedWithNoPo(amountByField.get("realizedWithNoPo"));
    budget.setRealizedWithPo(amountByField.get("realizedWithPo"));
    budget.setAvailableAmount(amountByField.get("totalAmountAvailable"));
    budget.setTotalFirmGap(amountByField.get("totalFirmGap"));
    budget.setSimulatedAmount(amountByField.get("simulatedAmount"));
    budget.setAvailableAmountWithSimulated(amountByField.get("availableAmountWithSimulated"));
  }
}
