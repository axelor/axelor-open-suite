package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.Order;
import com.axelor.apps.base.interfaces.OrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class SaleOrderShipmentCostLineCreationService extends ShipmentCostLineCreationService {
  protected SaleOrderLineService saleOrderLineService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public SaleOrderShipmentCostLineCreationService(
      SaleOrderLineService saleOrderLineService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderLineRepository saleOrderLineRepository) {
    this.saleOrderLineService = saleOrderLineService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  protected void setOrderLineList(Order order, List<? extends OrderLine> orderLines) {
    ((SaleOrder) order).setSaleOrderLineList((List<SaleOrderLine>) orderLines);
  }

  @Override
  protected void removeLine(OrderLine lineToRemove) {
    if (lineToRemove.getId() != null) {
      saleOrderLineRepository.remove((SaleOrderLine) lineToRemove);
    }
  }

  @Override
  protected List<? extends OrderLine> getOrderList(Order order) {
    return ((SaleOrder) order).getSaleOrderLineList();
  }

  @Override
  protected ShipmentMode getShipmentMode(Order order) {
    return ((SaleOrder) order).getShipmentMode();
  }

  @Override
  protected String process(Order order, ShipmentMode shipmentMode, Product shippingCostProduct)
      throws AxelorException {
    BigDecimal carriagePaidThreshold = shipmentMode.getCarriagePaidThreshold();
    Partner client = ((SaleOrder) order).getClientPartner();
    if (client != null) {
      List<CustomerShippingCarriagePaid> carriagePaids =
          client.getCustomerShippingCarriagePaidList();
      for (CustomerShippingCarriagePaid customerShippingCarriagePaid : carriagePaids) {
        if (shipmentMode.getId().equals(customerShippingCarriagePaid.getShipmentMode().getId())) {
          if (customerShippingCarriagePaid.getShippingCostsProduct() != null) {
            shippingCostProduct = customerShippingCarriagePaid.getShippingCostsProduct();
          }
          carriagePaidThreshold = customerShippingCarriagePaid.getCarriagePaidThreshold();
          break;
        }
      }
    }
    if (carriagePaidThreshold != null
        && shipmentMode.getHasCarriagePaidPossibility()
        && computeExTaxTotalWithoutShippingLines(order).compareTo(carriagePaidThreshold) >= 0) {
      String message = removeShipmentCostLine(order);
      saleOrderComputeService.computeSaleOrder((SaleOrder) order);
      saleOrderMarginService.computeMarginSaleOrder((SaleOrder) order);
      return message;
    }
    return null;
  }

  @Override
  protected OrderLine createShippingCostLine(Order order, Product shippingCostProduct)
      throws AxelorException {
    SaleOrderLine shippingCostLine = new SaleOrderLine();
    shippingCostLine.setSaleOrder((SaleOrder) order);
    shippingCostLine.setProduct(shippingCostProduct);
    saleOrderLineService.computeProductInformation(shippingCostLine, (SaleOrder) order);
    saleOrderLineService.computeValues((SaleOrder) order, shippingCostLine);
    return shippingCostLine;
  }

  @Override
  protected void addAndCompute(Order order, List<OrderLine> orderLines, Product shippingCostProduct)
      throws AxelorException {
    SaleOrderLine shippingCostLine =
        (SaleOrderLine) createShippingCostLine(order, shippingCostProduct);
    orderLines.add(shippingCostLine);
    saleOrderComputeService.computeSaleOrder((SaleOrder) order);
    saleOrderMarginService.computeMarginSaleOrder((SaleOrder) order);
  }
}
