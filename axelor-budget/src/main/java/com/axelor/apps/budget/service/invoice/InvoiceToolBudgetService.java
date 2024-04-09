package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.budget.db.BudgetDistribution;
import java.math.BigDecimal;
import java.util.List;

public interface InvoiceToolBudgetService {

  void copyBudgetDistributionList(
      List<BudgetDistribution> originalBudgetDistributionList,
      InvoiceLine invoiceLine,
      BigDecimal prorata);
}
