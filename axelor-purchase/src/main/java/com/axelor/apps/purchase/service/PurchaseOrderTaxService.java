package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderTaxService {
  void setPurchaseOrderInAti(PurchaseOrder purchaseOrder) throws AxelorException;
}
