package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceToolBudgetServiceImpl implements InvoiceToolBudgetService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public InvoiceToolBudgetServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void copyBudgetDistributionList(
      List<BudgetDistribution> originalBudgetDistributionList,
      InvoiceLine invoiceLine,
      BigDecimal prorata) {

    if (CollectionUtils.isEmpty(originalBudgetDistributionList)) {
      return;
    }

    for (BudgetDistribution budgetDistributionIt : originalBudgetDistributionList) {
      BudgetDistribution budgetDistribution = new BudgetDistribution();
      budgetDistribution.setBudget(budgetDistributionIt.getBudget());
      budgetDistribution.setAmount(
          currencyScaleService.getCompanyScaledValue(
              budgetDistributionIt, budgetDistributionIt.getAmount().multiply(prorata)));
      budgetDistribution.setBudgetAmountAvailable(budgetDistributionIt.getBudgetAmountAvailable());
      invoiceLine.addBudgetDistributionListItem(budgetDistribution);
    }
  }
}
