package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.PricedOrder;
import com.axelor.apps.stock.db.ShipmentMode;

public interface PurchaseOrderShipmentService {
  String createShipmentCostLine(PricedOrder pricedOrder, ShipmentMode shipmentMode)
      throws AxelorException;
}
