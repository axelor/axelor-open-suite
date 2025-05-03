package com.axelor.apps.budget.service.date;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import java.time.LocalDate;
import java.util.List;

public interface BudgetDateService {
  String checkBudgetDates(Invoice invoice);

  String checkBudgetDates(Move move);

  String checkBudgetDates(PurchaseOrder purchaseOrder);

  String checkBudgetDates(SaleOrder saleOrder);

  String getBudgetDateError(
      LocalDate fromDate,
      LocalDate toDate,
      Budget budget,
      List<BudgetDistribution> budgetDistributionList);
}
