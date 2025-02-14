package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.ShippableOrder;
import com.axelor.apps.stock.db.ShipmentMode;

public interface PurchaseOrderShipmentService {
  String createShipmentCostLine(ShippableOrder shippableOrder, ShipmentMode shipmentMode)
      throws AxelorException;
}
