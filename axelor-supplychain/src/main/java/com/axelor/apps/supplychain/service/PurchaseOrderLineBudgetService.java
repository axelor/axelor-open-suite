package com.axelor.apps.supplychain.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;

public interface PurchaseOrderLineBudgetService {
  void computeBudgetDistributionSumAmount(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder);
}
