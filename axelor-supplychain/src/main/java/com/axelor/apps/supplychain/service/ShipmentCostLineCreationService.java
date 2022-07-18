package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.Order;
import com.axelor.apps.base.interfaces.OrderLine;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class ShipmentCostLineCreationService {
  public String createShipmentCostLine(Order order) throws AxelorException {
    List<OrderLine> orderLineList = (List<OrderLine>) this.getOrderList(order);
    ShipmentMode shipmentMode = this.getShipmentMode(order);

    if (shipmentMode == null) {
      return null;
    }

    Product shippingCostProduct = shipmentMode.getShippingCostsProduct();
    if (shippingCostProduct == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.SHIPMENT_MODE_MISSING_SHIPPING_COST));
    }

    this.process(order, shipmentMode, shippingCostProduct);

    if (alreadyHasShippingCostLine(order, shippingCostProduct)) {
      return null;
    }

    this.addAndCompute(order, orderLineList, shippingCostProduct);
    return null;
  }

  protected boolean alreadyHasShippingCostLine(Order order, Product shippingCostProduct) {
    List<OrderLine> orderLines = (List<OrderLine>) this.getOrderList(order);
    if (orderLines == null) {
      return false;
    }
    for (OrderLine orderLine : orderLines) {
      if (shippingCostProduct.equals(orderLine.getProduct())) {
        return true;
      }
    }
    return false;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected String removeShipmentCostLine(Order order) {
    List<OrderLine> orderLines = (List<OrderLine>) this.getOrderList(order);
    if (orderLines == null) {
      return null;
    }
    List<OrderLine> linesToRemove = new ArrayList<>();
    for (OrderLine orderLine : orderLines) {
      if (orderLine.getProduct().getIsShippingCostsProduct()) {
        linesToRemove.add(orderLine);
      }
    }
    if (linesToRemove.isEmpty()) {
      return null;
    }

    for (OrderLine lineToRemove : linesToRemove) {
      orderLines.remove(lineToRemove);
      this.removeLine(lineToRemove);
    }

    setOrderLineList(order, orderLines);
    return I18n.get("Carriage paid threshold is exceeded, all shipment cost lines are removed");
  }

  protected BigDecimal computeExTaxTotalWithoutShippingLines(Order order) {
    List<OrderLine> orderLines = (List<OrderLine>) this.getOrderList(order);
    if (orderLines == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal exTaxTotal = BigDecimal.ZERO;
    for (OrderLine orderLine : orderLines) {
      if (!orderLine.getProduct().getIsShippingCostsProduct()) {
        exTaxTotal = exTaxTotal.add(orderLine.getExTaxTotal());
      }
    }
    return exTaxTotal;
  }

  protected abstract void setOrderLineList(Order order, List<? extends OrderLine> orderLines);

  protected abstract void removeLine(OrderLine orderLine);

  protected abstract List<? extends OrderLine> getOrderList(Order order);

  protected abstract ShipmentMode getShipmentMode(Order order);

  protected abstract String process(
      Order order, ShipmentMode shipmentMode, Product shippingCostProduct) throws AxelorException;

  protected abstract OrderLine createShippingCostLine(Order order, Product shippingCostProduct)
      throws AxelorException;

  protected abstract void addAndCompute(
      Order order, List<OrderLine> orderLines, Product shippingCostProduct) throws AxelorException;
}
