package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;

public interface AnalyticToolSupplychainService {

  void checkSaleOrderLinesAnalyticDistribution(SaleOrder saleOrder) throws AxelorException;

  void checkPurchaseOrderLinesAnalyticDistribution(PurchaseOrder purchaseOrder)
      throws AxelorException;
}
