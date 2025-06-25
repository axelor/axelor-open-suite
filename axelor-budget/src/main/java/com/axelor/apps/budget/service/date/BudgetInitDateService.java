package com.axelor.apps.budget.service.date;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;

public interface BudgetInitDateService {
  void initializeBudgetDates(Invoice invoice) throws AxelorException;

  void initializeBudgetDates(PurchaseOrder purchaseOrder) throws AxelorException;

  void initializeBudgetDates(SaleOrder saleOrder) throws AxelorException;

  void initializeBudgetDates(Move move) throws AxelorException;
}
