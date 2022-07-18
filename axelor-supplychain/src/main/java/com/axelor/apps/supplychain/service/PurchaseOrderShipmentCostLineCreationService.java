package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.Order;
import com.axelor.apps.base.interfaces.OrderLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.List;

public class PurchaseOrderShipmentCostLineCreationService extends ShipmentCostLineCreationService {
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected PurchaseOrderService purchaseOrderService;
  protected PurchaseOrderLineRepository purchaseOrderLineRepository;

  @Inject
  public PurchaseOrderShipmentCostLineCreationService(
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderLineRepository purchaseOrderLineRepository) {
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
  }

  @Override
  protected void setOrderLineList(Order order, List<? extends OrderLine> orderLines) {
    ((PurchaseOrder) order).setPurchaseOrderLineList((List<PurchaseOrderLine>) orderLines);
  }

  @Override
  protected void removeLine(OrderLine lineToRemove) {
    if (lineToRemove.getId() != null) {
      purchaseOrderLineRepository.remove((PurchaseOrderLine) lineToRemove);
    }
  }

  @Override
  protected List<? extends OrderLine> getOrderList(Order order) {
    return ((PurchaseOrder) order).getPurchaseOrderLineList();
  }

  @Override
  protected ShipmentMode getShipmentMode(Order order) {
    return ((PurchaseOrder) order).getShipmentMode();
  }

  @Override
  protected String process(Order order, ShipmentMode shipmentMode, Product shippingCostProduct)
      throws AxelorException {
    if (shipmentMode.getHasCarriagePaidPossibility()
        && computeExTaxTotalWithoutShippingLines(order)
                .compareTo(shipmentMode.getCarriagePaidThreshold())
            >= 0) {
      String message = removeShipmentCostLine(order);
      purchaseOrderService.computePurchaseOrder((PurchaseOrder) order);
      return message;
    }
    return null;
  }

  @Override
  protected OrderLine createShippingCostLine(Order order, Product shippingCostProduct)
      throws AxelorException {
    PurchaseOrderLine shippingCostLine = new PurchaseOrderLine();
    shippingCostLine.setPurchaseOrder((PurchaseOrder) order);
    shippingCostLine.setProduct(shippingCostProduct);
    purchaseOrderLineService.fill(shippingCostLine, (PurchaseOrder) order);
    purchaseOrderLineService.compute(shippingCostLine, (PurchaseOrder) order);
    return shippingCostLine;
  }

  @Override
  protected void addAndCompute(Order order, List<OrderLine> orderLines, Product shippingCostProduct)
      throws AxelorException {
    PurchaseOrderLine shippingCostLine =
        (PurchaseOrderLine) createShippingCostLine(order, shippingCostProduct);
    orderLines.add(shippingCostLine);
    purchaseOrderService.computePurchaseOrder((PurchaseOrder) order);
  }
}
