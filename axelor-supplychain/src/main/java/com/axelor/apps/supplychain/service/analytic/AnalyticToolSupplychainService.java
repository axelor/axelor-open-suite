package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;

public interface AnalyticToolSupplychainService {

  void checkSaleOrderLinesAnalyticDistribution(SaleOrder saleOrder) throws AxelorException;

  void checkPurchaseOrderLinesAnalyticDistribution(PurchaseOrder purchaseOrder)
      throws AxelorException;
}
