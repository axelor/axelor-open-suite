package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;

public interface ShippingService {
  Product getShippingCostProduct(
      ShipmentMode shipmentMode, CustomerShippingCarriagePaid customerShippingCarriagePaid);
}
