package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderShipmentService {
  String createShipmentCostLine(PurchaseOrder purchaseOrder) throws AxelorException;
}
