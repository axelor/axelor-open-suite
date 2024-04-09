package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.budget.db.BudgetDistribution;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceToolBudgetServiceImpl implements InvoiceToolBudgetService {

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
          budgetDistributionIt.getAmount().multiply(prorata).setScale(2, RoundingMode.HALF_UP));
      budgetDistribution.setBudgetAmountAvailable(budgetDistributionIt.getBudgetAmountAvailable());
      invoiceLine.addBudgetDistributionListItem(budgetDistribution);
    }
  }
}
