package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface BudgetAmountToolService {
  BigDecimal getBudgetMaxAmount(PurchaseOrderLine purchaseOrderLine);

  BigDecimal getBudgetMaxAmount(SaleOrderLine saleOrderLine);

  BigDecimal getBudgetMaxAmount(InvoiceLine invoiceLine) throws AxelorException;

  boolean manageTaxAmounts(Invoice invoice) throws AxelorException;

  BigDecimal getBudgetMaxAmount(MoveLine moveLine);
}
