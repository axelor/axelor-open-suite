package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.exception.AxelorException;

public interface PurchaseOrderWorkflowService {

  void draftPurchaseOrder(PurchaseOrder purchaseOrder);

  void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  void finishPurchaseOrder(PurchaseOrder purchaseOrder);

  void cancelPurchaseOrder(PurchaseOrder purchaseOrder);
}
