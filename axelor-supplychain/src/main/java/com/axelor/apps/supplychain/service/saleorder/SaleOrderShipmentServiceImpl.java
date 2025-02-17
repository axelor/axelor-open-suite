/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.ShippableOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.ShippingAbstractService;
import com.axelor.apps.supplychain.service.ShippingService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderShipmentServiceImpl extends ShippingAbstractService
    implements SaleOrderShipmentService {

  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService;

  @Inject
  public SaleOrderShipmentServiceImpl(
      ShippingService shippingService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService) {
    super(shippingService);
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.saleOrderLineOnProductChangeService = saleOrderLineOnProductChangeService;
  }

  @Override
  protected void addLineAndComputeOrder(ShippableOrder shippableOrder, Product shippingCostProduct)
      throws AxelorException {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    if (saleOrder == null) {
      return;
    }
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    saleOrderLines.add(createShippingCostLine(saleOrder, shippingCostProduct));
    computeSaleOrder(saleOrder);
  }

  @Override
  protected String removeLineAndComputeOrder(ShippableOrder shippableOrder) throws AxelorException {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    if (saleOrder == null) {
      return null;
    }
    String message = removeShipmentCostLine(saleOrder);
    computeSaleOrder(saleOrder);
    return message;
  }

  protected void computeSaleOrder(SaleOrder saleOrder) throws AxelorException {
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderMarginService.computeMarginSaleOrder(saleOrder);
  }

  @Override
  protected CustomerShippingCarriagePaid getShippingCarriagePaid(
      ShippableOrder shippableOrder, ShipmentMode shipmentMode) {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    if (saleOrder == null) {
      return null;
    }
    Partner client = saleOrder.getClientPartner();
    return client.getCustomerShippingCarriagePaidList().stream()
        .filter(carriage -> carriage.getShipmentMode().equals(shipmentMode))
        .findFirst()
        .orElse(null);
  }

  @Override
  protected SaleOrderLine createShippingCostLine(
      ShippableOrder shippableOrder, Product shippingCostProduct) throws AxelorException {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    if (saleOrder == null) {
      return null;
    }
    SaleOrderLine shippingCostLine = new SaleOrderLine();
    shippingCostLine.setSaleOrder(saleOrder);
    shippingCostLine.setProduct(shippingCostProduct);
    saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrder, shippingCostLine);
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
    return I18n.get(SupplychainExceptionMessage.SHIPMENT_THRESHOLD_EXCEEDED);
  }

  protected SaleOrder getSaleOrder(ShippableOrder shippableOrder) {
    SaleOrder saleOrder = null;
    if (shippableOrder instanceof SaleOrder) {
      saleOrder = (SaleOrder) shippableOrder;
    }
    return saleOrder;
  }
}
