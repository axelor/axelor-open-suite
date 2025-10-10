package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderChangeValidationSupplychainService {

  void validatePurchaseOrderChange(PurchaseOrder purchaseOrder) throws AxelorException;
}
