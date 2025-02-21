package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.ShippableOrder;
import com.axelor.apps.base.interfaces.ShippableOrderLine;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    List<CustomerShippingCarriagePaid> customerShippingCarriagePaidList =
        getShippingCarriagePaidList(shippableOrder);

    CustomerShippingCarriagePaid supplierShippingCarriagePaid =
        customerShippingCarriagePaidList.stream()
            .filter(carriage -> carriage.getShipmentMode().equals(shipmentMode))
            .findFirst()
            .orElse(null);

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

  @Transactional(rollbackOn = Exception.class)
  protected String removeShipmentCostLine(ShippableOrder shippableOrder) {
    List<ShippableOrderLine> linesToRemove = new ArrayList<>();
    for (ShippableOrderLine shippableOrderLine : getShippableOrderLineList(shippableOrder)) {
      if (shippableOrderLine.getProduct() != null
          && shippableOrderLine.getProduct().getIsShippingCostsProduct()) {
        linesToRemove.add(shippableOrderLine);
      }
    }
    if (linesToRemove.isEmpty()) {
      return null;
    }
    removeShippableOrderLineList(shippableOrder, linesToRemove);
    return I18n.get(SupplychainExceptionMessage.SHIPMENT_THRESHOLD_EXCEEDED);
  }

  protected abstract void removeShippableOrderLineList(
      ShippableOrder shippableOrder, List<ShippableOrderLine> shippableOrderLinesToRemove);

  protected abstract List<CustomerShippingCarriagePaid> getShippingCarriagePaidList(
      ShippableOrder shippableOrder);

  protected abstract List<? extends ShippableOrderLine> getShippableOrderLineList(
      ShippableOrder shippableOrder);

  protected abstract String removeLineAndComputeOrder(ShippableOrder shippableOrder)
      throws AxelorException;

  protected abstract void addLineAndComputeOrder(
      ShippableOrder shippableOrder, Product shippingCostProduct) throws AxelorException;
}
