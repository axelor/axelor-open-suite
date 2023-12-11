package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderShipmentServiceImpl implements SaleOrderShipmentService {

  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected SaleOrderLineService saleOrderLineService;
  protected SaleOrderLineRepository saleOrderLineRepo;

  @Inject
  public SaleOrderShipmentServiceImpl(
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderLineService saleOrderLineService,
      SaleOrderLineRepository saleOrderLineRepo) {
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.saleOrderLineService = saleOrderLineService;
    this.saleOrderLineRepo = saleOrderLineRepo;
  }

  @Override
  public String createShipmentCostLine(SaleOrder saleOrder) throws AxelorException {
    ShipmentMode shipmentMode = saleOrder.getShipmentMode();
    if (shipmentMode == null) {
      return null;
    }

    CustomerShippingCarriagePaid customerShippingCarriagePaid =
        getClientCustomerShippingCarriagePaid(saleOrder, shipmentMode);
    Product shippingCostProduct =
        getShippingCostProduct(shipmentMode, customerShippingCarriagePaid);
    if (shippingCostProduct == null) {
      return null;
    }

    if (isThresholdUsedAndExceeded(saleOrder, shipmentMode, customerShippingCarriagePaid)) {
      return removeLineAndComputeOrder(saleOrder);
    }

    if (alreadyHasShippingCostLine(saleOrder, shippingCostProduct)) {
      return null;
    }

    addLineAndComputeOrder(saleOrder, shippingCostProduct);
    return null;
  }

  protected boolean isThresholdUsedAndExceeded(
      SaleOrder saleOrder,
      ShipmentMode shipmentMode,
      CustomerShippingCarriagePaid customerShippingCarriagePaid) {
    BigDecimal carriagePaidThreshold =
        getCarriagePaidThreshold(shipmentMode, customerShippingCarriagePaid);
    return carriagePaidThreshold != null
        && shipmentMode.getHasCarriagePaidPossibility()
        && computeExTaxTotalWithoutShippingLines(saleOrder).compareTo(carriagePaidThreshold) >= 0;
  }

  protected void addLineAndComputeOrder(SaleOrder saleOrder, Product shippingCostProduct)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    saleOrderLines.add(createShippingCostLine(saleOrder, shippingCostProduct));
    computeSaleOrder(saleOrder);
  }

  protected String removeLineAndComputeOrder(SaleOrder saleOrder) throws AxelorException {
    String message = removeShipmentCostLine(saleOrder);
    computeSaleOrder(saleOrder);
    return message;
  }

  protected void computeSaleOrder(SaleOrder saleOrder) throws AxelorException {
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderMarginService.computeMarginSaleOrder(saleOrder);
  }

  protected BigDecimal getCarriagePaidThreshold(
      ShipmentMode shipmentMode, CustomerShippingCarriagePaid customerShippingCarriagePaid) {
    BigDecimal carriagePaidThreshold = shipmentMode.getCarriagePaidThreshold();
    if (customerShippingCarriagePaid != null) {
      carriagePaidThreshold = customerShippingCarriagePaid.getCarriagePaidThreshold();
    }
    return carriagePaidThreshold;
  }

  protected Product getShippingCostProduct(
      ShipmentMode shipmentMode, CustomerShippingCarriagePaid customerShippingCarriagePaid) {
    Product shippingCostProduct = shipmentMode.getShippingCostsProduct();
    if (customerShippingCarriagePaid != null
        && customerShippingCarriagePaid.getShippingCostsProduct() != null) {
      shippingCostProduct = customerShippingCarriagePaid.getShippingCostsProduct();
    }
    return shippingCostProduct;
  }

  protected CustomerShippingCarriagePaid getClientCustomerShippingCarriagePaid(
      SaleOrder saleOrder, ShipmentMode shipmentMode) {
    Partner client = saleOrder.getClientPartner();
    return client.getCustomerShippingCarriagePaidList().stream()
        .filter(carriage -> carriage.getShipmentMode().equals(shipmentMode))
        .findFirst()
        .orElse(null);
  }

  protected boolean alreadyHasShippingCostLine(SaleOrder saleOrder, Product shippingCostProduct) {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    if (saleOrderLines == null) {
      return false;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLines) {
      if (shippingCostProduct.equals(saleOrderLine.getProduct())) {
        return true;
      }
    }
    return false;
  }

  protected SaleOrderLine createShippingCostLine(SaleOrder saleOrder, Product shippingCostProduct)
      throws AxelorException {
    SaleOrderLine shippingCostLine = new SaleOrderLine();
    shippingCostLine.setSaleOrder(saleOrder);
    shippingCostLine.setProduct(shippingCostProduct);
    saleOrderLineService.computeProductInformation(shippingCostLine, saleOrder);
    saleOrderLineService.computeValues(saleOrder, shippingCostLine);
    return shippingCostLine;
  }

  @Transactional(rollbackOn = Exception.class)
  protected String removeShipmentCostLine(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    if (saleOrderLines == null) {
      return null;
    }
    List<SaleOrderLine> linesToRemove = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrderLines) {
      if (saleOrderLine.getProduct().getIsShippingCostsProduct()) {
        linesToRemove.add(saleOrderLine);
      }
    }
    if (linesToRemove.isEmpty()) {
      return null;
    }
    for (SaleOrderLine lineToRemove : linesToRemove) {
      saleOrderLines.remove(lineToRemove);
      if (lineToRemove.getId() != null) {
        saleOrderLineRepo.remove(lineToRemove);
      }
    }
    saleOrder.setSaleOrderLineList(saleOrderLines);
    return I18n.get(SupplychainExceptionMessage.SALE_SHIPMENT_THRESHOLD_EXCEEDED);
  }

  protected BigDecimal computeExTaxTotalWithoutShippingLines(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    if (saleOrderLines == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal exTaxTotal = BigDecimal.ZERO;
    for (SaleOrderLine saleOrderLine : saleOrderLines) {
      if (!saleOrderLine.getProduct().getIsShippingCostsProduct()) {
        exTaxTotal = exTaxTotal.add(saleOrderLine.getExTaxTotal());
      }
    }
    return exTaxTotal;
  }
}
