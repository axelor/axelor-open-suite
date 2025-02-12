package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderShipmentServiceImpl implements PurchaseOrderShipmentService {

  protected final PurchaseOrderService purchaseOrderService;
  protected final PurchaseOrderLineService purchaseOrderLineService;
  protected final PurchaseOrderLineRepository purchaseOrderLineRepository;
  protected final ShippingService shippingService;

  @Inject
  public PurchaseOrderShipmentServiceImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      ShippingService shippingService) {
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.shippingService = shippingService;
  }

  @Override
  public String createShipmentCostLine(PurchaseOrder purchaseOrder) throws AxelorException {
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    ShipmentMode shipmentMode = purchaseOrder.getShipmentMode();
    if (shipmentMode == null) {
      return null;
    }

    CustomerShippingCarriagePaid supplierShippingCarriagePaid =
        getSupplierShippingCarriagePaid(purchaseOrder, shipmentMode);
    Product shippingCostProduct =
        shippingService.getShippingCostProduct(shipmentMode, supplierShippingCarriagePaid);
    if (shippingCostProduct == null) {
      return null;
    }

    if (isThresholdUsedAndExceeded(purchaseOrder, shipmentMode, supplierShippingCarriagePaid)) {
      return removeLineAndComputeOrder(purchaseOrder);
    }

    if (alreadyHasShippingCostLine(purchaseOrder, shippingCostProduct)) {
      return null;
    }

    addLineAndComputeOrder(purchaseOrder, shippingCostProduct, purchaseOrderLines);
    return null;
  }

  protected boolean isThresholdUsedAndExceeded(
      PurchaseOrder purchaseOrder,
      ShipmentMode shipmentMode,
      CustomerShippingCarriagePaid customerShippingCarriagePaid) {
    BigDecimal carriagePaidThreshold =
        getCarriagePaidThreshold(shipmentMode, customerShippingCarriagePaid);
    return carriagePaidThreshold != null
        && shipmentMode.getHasCarriagePaidPossibility()
        && computeExTaxTotalWithoutShippingLines(purchaseOrder).compareTo(carriagePaidThreshold)
            >= 0;
  }

  protected BigDecimal getCarriagePaidThreshold(
      ShipmentMode shipmentMode, CustomerShippingCarriagePaid customerShippingCarriagePaid) {
    BigDecimal carriagePaidThreshold = shipmentMode.getCarriagePaidThreshold();
    if (customerShippingCarriagePaid != null) {
      carriagePaidThreshold = customerShippingCarriagePaid.getCarriagePaidThreshold();
    }
    return carriagePaidThreshold;
  }

  protected void addLineAndComputeOrder(
      PurchaseOrder purchaseOrder,
      Product shippingCostProduct,
      List<PurchaseOrderLine> purchaseOrderLines)
      throws AxelorException {
    PurchaseOrderLine shippingCostLine = createShippingCostLine(purchaseOrder, shippingCostProduct);
    purchaseOrderLines.add(shippingCostLine);
    purchaseOrderService.computePurchaseOrder(purchaseOrder);
  }

  protected String removeLineAndComputeOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    String message = removeShipmentCostLine(purchaseOrder);
    purchaseOrderService.computePurchaseOrder(purchaseOrder);
    return message;
  }

  protected CustomerShippingCarriagePaid getSupplierShippingCarriagePaid(
      PurchaseOrder purchaseOrder, ShipmentMode shipmentMode) {
    Partner client = purchaseOrder.getSupplierPartner();
    return client.getSupplierShippingCarriagePaidList().stream()
        .filter(carriage -> carriage.getShipmentMode().equals(shipmentMode))
        .findFirst()
        .orElse(null);
  }

  protected boolean alreadyHasShippingCostLine(
      PurchaseOrder purchaseOrder, Product shippingCostProduct) {
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLines == null) {
      return false;
    }
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLines) {
      if (shippingCostProduct.equals(purchaseOrderLine.getProduct())) {
        return true;
      }
    }
    return false;
  }

  protected PurchaseOrderLine createShippingCostLine(
      PurchaseOrder purchaseOrder, Product shippingCostProduct) throws AxelorException {
    PurchaseOrderLine shippingCostLine = new PurchaseOrderLine();
    shippingCostLine.setPurchaseOrder(purchaseOrder);
    shippingCostLine.setProduct(shippingCostProduct);
    purchaseOrderLineService.fill(shippingCostLine, purchaseOrder);
    purchaseOrderLineService.compute(shippingCostLine, purchaseOrder);
    return shippingCostLine;
  }

  @Transactional(rollbackOn = Exception.class)
  protected String removeShipmentCostLine(PurchaseOrder purchaseOrder) {
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLines == null) {
      return null;
    }
    List<PurchaseOrderLine> linesToRemove = new ArrayList<>();
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLines) {
      if (purchaseOrderLine.getProduct() != null
          && purchaseOrderLine.getProduct().getIsShippingCostsProduct()) {
        linesToRemove.add(purchaseOrderLine);
      }
    }
    if (linesToRemove.isEmpty()) {
      return null;
    }
    for (PurchaseOrderLine lineToRemove : linesToRemove) {
      purchaseOrderLines.remove(lineToRemove);
      if (lineToRemove.getId() != null) {
        purchaseOrderLineRepository.remove(lineToRemove);
      }
    }
    purchaseOrder.setPurchaseOrderLineList(purchaseOrderLines);
    return I18n.get("Carriage paid threshold is exceeded, all shipment cost lines are removed");
  }

  protected BigDecimal computeExTaxTotalWithoutShippingLines(PurchaseOrder purchaseOrder) {
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLines == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal exTaxTotal = BigDecimal.ZERO;
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLines) {
      if (purchaseOrderLine.getProduct() != null
          && !purchaseOrderLine.getProduct().getIsShippingCostsProduct()) {
        exTaxTotal = exTaxTotal.add(purchaseOrderLine.getExTaxTotal());
      }
    }
    return exTaxTotal;
  }
}
