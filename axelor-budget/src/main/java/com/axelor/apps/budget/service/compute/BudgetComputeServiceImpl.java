package com.axelor.apps.budget.service.compute;

import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.google.inject.Inject;
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
