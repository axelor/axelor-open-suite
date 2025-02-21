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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.ShippableOrder;
import com.axelor.apps.base.interfaces.ShippableOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineInitValueService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.axelor.apps.supplychain.service.ShippingAbstractService;
import com.axelor.apps.supplychain.service.ShippingService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderShipmentServiceImpl extends ShippingAbstractService
    implements SaleOrderShipmentService {

  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService;
  protected SaleOrderLineInitValueService saleOrderLineInitValueService;

  @Inject
  public SaleOrderShipmentServiceImpl(
      ShippingService shippingService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService,
      SaleOrderLineInitValueService saleOrderLineInitValueService) {
    super(shippingService);
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.saleOrderLineOnProductChangeService = saleOrderLineOnProductChangeService;
    this.saleOrderLineInitValueService = saleOrderLineInitValueService;
  }

  @Override
  protected void addLineAndComputeOrder(ShippableOrder shippableOrder, Product shippingCostProduct)
      throws AxelorException {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    if (saleOrder == null) {
      return;
    }
    saleOrder.addSaleOrderLineListItem(createShippingCostLine(saleOrder, shippingCostProduct));
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
  protected List<CustomerShippingCarriagePaid> getShippingCarriagePaidList(
      ShippableOrder shippableOrder) {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    if (saleOrder == null) {
      return new ArrayList<>();
    }
    return saleOrder.getClientPartner().getCustomerShippingCarriagePaidList();
  }

  protected SaleOrderLine createShippingCostLine(
      ShippableOrder shippableOrder, Product shippingCostProduct) throws AxelorException {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    if (saleOrder == null) {
      return null;
    }
    SaleOrderLine shippingCostLine = new SaleOrderLine();
    saleOrderLineInitValueService.onNewInitValues(saleOrder, shippingCostLine, null);
    shippingCostLine.setSaleOrder(saleOrder);
    shippingCostLine.setProduct(shippingCostProduct);
    saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrder, shippingCostLine);
    return shippingCostLine;
  }

  @Override
  protected void removeShippableOrderLineList(
      ShippableOrder shippableOrder, List<ShippableOrderLine> shippableOrderLinesToRemove) {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    for (ShippableOrderLine lineToRemove : shippableOrderLinesToRemove) {
      saleOrder.removeSaleOrderLineListItem((SaleOrderLine) lineToRemove);
      if (lineToRemove.getId() != null) {
        saleOrderLineRepo.remove(saleOrderLineRepo.find(lineToRemove.getId()));
      }
    }
  }

  protected List<? extends ShippableOrderLine> getShippableOrderLineList(
      ShippableOrder shippableOrder) {
    SaleOrder saleOrder = getSaleOrder(shippableOrder);
    if (saleOrder == null) {
      return null;
    }
    return saleOrder.getSaleOrderLineList();
  }

  protected SaleOrder getSaleOrder(ShippableOrder shippableOrder) {
    SaleOrder saleOrder = null;
    if (shippableOrder instanceof SaleOrder) {
      saleOrder = (SaleOrder) shippableOrder;
    }
    return saleOrder;
  }
}
