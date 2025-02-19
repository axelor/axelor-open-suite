package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.ShippableOrder;
import com.axelor.apps.base.interfaces.ShippableOrderLine;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public abstract class ShippingAbstractService {

  protected final ShippingService shippingService;

  @Inject
  protected ShippingAbstractService(ShippingService shippingService) {
    this.shippingService = shippingService;
  }

  public String createShipmentCostLine(ShippableOrder shippableOrder, ShipmentMode shipmentMode)
      throws AxelorException {
    if (shipmentMode == null) {
      return null;
    }

    CustomerShippingCarriagePaid supplierShippingCarriagePaid =
        getShippingCarriagePaid(shippableOrder, shipmentMode);
    Product shippingCostProduct =
        shippingService.getShippingCostProduct(shipmentMode, supplierShippingCarriagePaid);
    if (shippingCostProduct == null) {
      return null;
    }

    if (isThresholdUsedAndExceeded(shippableOrder, shipmentMode, supplierShippingCarriagePaid)) {
      return removeLineAndComputeOrder(shippableOrder);
    }

    if (alreadyHasShippingCostLine(shippableOrder, shippingCostProduct)) {
      return null;
    }

    addLineAndComputeOrder(shippableOrder, shippingCostProduct);
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

  protected boolean alreadyHasShippingCostLine(
      ShippableOrder shippableOrder, Product shippingCostProduct) {
    if (shippableOrder == null) {
      return false;
    }
    List<? extends ShippableOrderLine> shippableOrderLineList =
        getShippableOrderLineList(shippableOrder);
    if (CollectionUtils.isEmpty(shippableOrderLineList)) {
      return false;
    }

    return shippableOrderLineList.stream()
        .anyMatch(line -> shippingCostProduct.equals(line.getProduct()));
  }

  protected boolean isThresholdUsedAndExceeded(
      ShippableOrder shippableOrder,
      ShipmentMode shipmentMode,
      CustomerShippingCarriagePaid customerShippingCarriagePaid) {

    if (shippableOrder == null) {
      return false;
    }

    BigDecimal carriagePaidThreshold =
        getCarriagePaidThreshold(shipmentMode, customerShippingCarriagePaid);
    return carriagePaidThreshold != null
        && shipmentMode.getHasCarriagePaidPossibility()
        && computeExTaxTotalWithoutShippingLines(shippableOrder).compareTo(carriagePaidThreshold)
            >= 0;
  }

  protected BigDecimal computeExTaxTotalWithoutShippingLines(ShippableOrder shippableOrder) {
    if (shippableOrder == null) {
      return BigDecimal.ZERO;
    }

    List<? extends ShippableOrderLine> shippableOrderLineList =
        getShippableOrderLineList(shippableOrder);

    if (CollectionUtils.isEmpty(shippableOrderLineList)) {
      return BigDecimal.ZERO;
    }

    BigDecimal exTaxTotal = BigDecimal.ZERO;
    for (ShippableOrderLine shippableOrderLine : shippableOrderLineList) {
      if (shippableOrderLine.getProduct() != null
          && !shippableOrderLine.getProduct().getIsShippingCostsProduct()) {
        exTaxTotal = exTaxTotal.add(shippableOrderLine.getExTaxTotal());
      }
    }
    return exTaxTotal;
  }

  protected abstract List<? extends ShippableOrderLine> getShippableOrderLineList(
      ShippableOrder shippableOrder);

  protected abstract CustomerShippingCarriagePaid getShippingCarriagePaid(
      ShippableOrder shippableOrder, ShipmentMode shipmentMode);

  protected abstract String removeLineAndComputeOrder(ShippableOrder shippableOrder)
      throws AxelorException;

  protected abstract void addLineAndComputeOrder(
      ShippableOrder shippableOrder, Product shippingCostProduct) throws AxelorException;
}
