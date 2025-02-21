package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;

public class ShippingServiceImpl implements ShippingService {

  @Override
  public Product getShippingCostProduct(
      ShipmentMode shipmentMode, CustomerShippingCarriagePaid customerShippingCarriagePaid) {
    Product shippingCostProduct = shipmentMode.getShippingCostsProduct();
    if (customerShippingCarriagePaid != null
        && customerShippingCarriagePaid.getShippingCostsProduct() != null) {
      shippingCostProduct = customerShippingCarriagePaid.getShippingCostsProduct();
    }
    return shippingCostProduct;
  }
}
