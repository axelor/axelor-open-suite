package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderReceiptStateService {
  void updateReceiptState(PurchaseOrder purchaseOrder) throws AxelorException;

  void updatePurchaseOrderLineReceiptState(PurchaseOrder purchaseOrder) throws AxelorException;
}
