package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.PricedOrder;
import com.axelor.apps.base.interfaces.PricedOrderLine;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.google.inject.Inject;
import java.math.BigDecimal;

public abstract class ShippingAbstractService {

  protected final ShippingService shippingService;

  @Inject
  protected ShippingAbstractService(ShippingService shippingService) {
    this.shippingService = shippingService;
  }

  public String createShipmentCostLine(PricedOrder pricedOrder, ShipmentMode shipmentMode)
      throws AxelorException {
    if (shipmentMode == null) {
      return null;
    }

    CustomerShippingCarriagePaid supplierShippingCarriagePaid =
        getShippingCarriagePaid(pricedOrder, shipmentMode);
    Product shippingCostProduct =
        shippingService.getShippingCostProduct(shipmentMode, supplierShippingCarriagePaid);
    if (shippingCostProduct == null) {
      return null;
    }

    if (isThresholdUsedAndExceeded(pricedOrder, shipmentMode, supplierShippingCarriagePaid)) {
      return removeLineAndComputeOrder(pricedOrder);
    }

    if (alreadyHasShippingCostLine(pricedOrder, shippingCostProduct)) {
      return null;
    }

    addLineAndComputeOrder(pricedOrder, shippingCostProduct);
    return null;
  }

  protected BigDecimal getCarriagePaidThreshold(
      ShipmentMode shipmentMode, CustomerShippingCarriagePaid customerShippingCarriagePaid) {
    BigDecimal carriagePaidThreshold = shipmentMode.getCarriagePaidThreshold();
    if (customerShippingCarriagePaid != null) {
      carriagePaidThreshold = customerShippingCarriagePaid.getCarriagePaidThreshold();
    }
    return carriagePaidThreshold;
  }

  protected abstract CustomerShippingCarriagePaid getShippingCarriagePaid(
      PricedOrder pricedOrder, ShipmentMode shipmentMode);

  protected abstract boolean alreadyHasShippingCostLine(
      PricedOrder pricedOrder, Product shippingCostProduct);

  protected abstract boolean isThresholdUsedAndExceeded(
      PricedOrder pricedOrder,
      ShipmentMode shipmentMode,
      CustomerShippingCarriagePaid customerShippingCarriagePaid);

  protected abstract String removeLineAndComputeOrder(PricedOrder pricedOrder)
      throws AxelorException;

  protected abstract PricedOrderLine createShippingCostLine(
      PricedOrder pricedOrder, Product shippingCostProduct) throws AxelorException;

  protected abstract void addLineAndComputeOrder(
      PricedOrder pricedOrder, Product shippingCostProduct) throws AxelorException;
}
