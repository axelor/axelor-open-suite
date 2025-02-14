package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.PricedOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderShipmentServiceImpl extends ShippingAbstractService
    implements PurchaseOrderShipmentService {

  protected final PurchaseOrderService purchaseOrderService;
  protected final PurchaseOrderLineService purchaseOrderLineService;
  protected final PurchaseOrderLineRepository purchaseOrderLineRepository;

  @Inject
  public PurchaseOrderShipmentServiceImpl(
      ShippingService shippingService,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      ShippingService shippingService1) {
    super(shippingService);
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
  }

  @Override
  protected boolean isThresholdUsedAndExceeded(
      PricedOrder pricedOrder,
      ShipmentMode shipmentMode,
      CustomerShippingCarriagePaid customerShippingCarriagePaid) {
    PurchaseOrder purchaseOrder = getPurchaseOrder(pricedOrder);
    if (purchaseOrder == null) {
      return false;
    }
    BigDecimal carriagePaidThreshold =
        getCarriagePaidThreshold(shipmentMode, customerShippingCarriagePaid);
    return carriagePaidThreshold != null
        && shipmentMode.getHasCarriagePaidPossibility()
        && computeExTaxTotalWithoutShippingLines(purchaseOrder).compareTo(carriagePaidThreshold)
            >= 0;
  }

  protected void addLineAndComputeOrder(PricedOrder pricedOrder, Product shippingCostProduct)
      throws AxelorException {
    PurchaseOrder purchaseOrder = getPurchaseOrder(pricedOrder);
    if (purchaseOrder == null) {
      return;
    }
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    purchaseOrderLines.add(createShippingCostLine(purchaseOrder, shippingCostProduct));
    purchaseOrderService.computePurchaseOrder(purchaseOrder);
  }

  @Override
  protected String removeLineAndComputeOrder(PricedOrder pricedOrder) throws AxelorException {
    PurchaseOrder purchaseOrder = getPurchaseOrder(pricedOrder);
    if (purchaseOrder == null) {
      return null;
    }
    String message = removeShipmentCostLine(purchaseOrder);
    purchaseOrderService.computePurchaseOrder(purchaseOrder);
    return message;
  }

  @Override
  protected CustomerShippingCarriagePaid getShippingCarriagePaid(
      PricedOrder pricedOrder, ShipmentMode shipmentMode) {
    PurchaseOrder purchaseOrder = getPurchaseOrder(pricedOrder);
    if (purchaseOrder == null) {
      return null;
    }
    Partner client = purchaseOrder.getSupplierPartner();
    return client.getSupplierShippingCarriagePaidList().stream()
        .filter(carriage -> carriage.getShipmentMode().equals(shipmentMode))
        .findFirst()
        .orElse(null);
  }

  @Override
  protected boolean alreadyHasShippingCostLine(
      PricedOrder pricedOrder, Product shippingCostProduct) {
    PurchaseOrder purchaseOrder = getPurchaseOrder(pricedOrder);
    if (purchaseOrder == null) {
      return false;
    }
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

  @Override
  protected PurchaseOrderLine createShippingCostLine(
      PricedOrder pricedOrder, Product shippingCostProduct) throws AxelorException {
    PurchaseOrder purchaseOrder = getPurchaseOrder(pricedOrder);
    if (purchaseOrder == null) {
      return null;
    }
    PurchaseOrderLine shippingCostLine = new PurchaseOrderLine();
    shippingCostLine.setPurchaseOrder(purchaseOrder);
    shippingCostLine.setProduct(shippingCostProduct);
    purchaseOrderLineService.fill(shippingCostLine, purchaseOrder);
    purchaseOrderLineService.compute(shippingCostLine, purchaseOrder);
    return shippingCostLine;
  }

  @Transactional(rollbackOn = Exception.class)
  protected String removeShipmentCostLine(PricedOrder pricedOrder) {
    PurchaseOrder purchaseOrder = getPurchaseOrder(pricedOrder);
    if (purchaseOrder == null) {
      return null;
    }
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
    return I18n.get(SupplychainExceptionMessage.SHIPMENT_THRESHOLD_EXCEEDED);
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

  protected PurchaseOrder getPurchaseOrder(PricedOrder pricedOrder) {
    PurchaseOrder purchaseOrder = null;
    if (pricedOrder instanceof PurchaseOrder) {
      purchaseOrder = (PurchaseOrder) pricedOrder;
    }
    return purchaseOrder;
  }
}
