package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;

public interface InvoiceTaxService {
  void manageTaxByAmount(SaleOrder saleOrder, Invoice invoice) throws AxelorException;

  void manageTaxByAmount(PurchaseOrder purchaseOrder, Invoice invoice) throws AxelorException;
}
