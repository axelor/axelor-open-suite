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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.interfaces.ShippableOrder;
import com.axelor.apps.base.interfaces.ShippableOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.google.inject.Inject;
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
      PurchaseOrderLineRepository purchaseOrderLineRepository) {
    super(shippingService);
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
  }

  @Override
  protected void addLineAndComputeOrder(ShippableOrder shippableOrder, Product shippingCostProduct)
      throws AxelorException {
    PurchaseOrder purchaseOrder = getPurchaseOrder(shippableOrder);
    if (purchaseOrder == null) {
      return;
    }
    purchaseOrder.addPurchaseOrderLineListItem(
        createShippingCostLine(purchaseOrder, shippingCostProduct));
    purchaseOrderService.computePurchaseOrder(purchaseOrder);
  }

  @Override
  protected String removeLineAndComputeOrder(ShippableOrder shippableOrder) throws AxelorException {
    PurchaseOrder purchaseOrder = getPurchaseOrder(shippableOrder);
    if (purchaseOrder == null) {
      return null;
    }
    String message = removeShipmentCostLine(purchaseOrder);
    purchaseOrderService.computePurchaseOrder(purchaseOrder);
    return message;
  }

  @Override
  protected List<CustomerShippingCarriagePaid> getShippingCarriagePaidList(
      ShippableOrder shippableOrder) {
    PurchaseOrder purchaseOrder = getPurchaseOrder(shippableOrder);
    if (purchaseOrder == null) {
      return new ArrayList<>();
    }
    return purchaseOrder.getSupplierPartner().getSupplierShippingCarriagePaidList();
  }

  protected PurchaseOrderLine createShippingCostLine(
      ShippableOrder shippableOrder, Product shippingCostProduct) throws AxelorException {
    PurchaseOrder purchaseOrder = getPurchaseOrder(shippableOrder);
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

  @Override
  protected void removeShippableOrderLineList(
      ShippableOrder shippableOrder, List<ShippableOrderLine> shippableOrderLinesToRemove) {
    PurchaseOrder purchaseOrder = getPurchaseOrder(shippableOrder);
    for (ShippableOrderLine lineToRemove : shippableOrderLinesToRemove) {
      purchaseOrder.removePurchaseOrderLineListItem((PurchaseOrderLine) lineToRemove);
      if (lineToRemove.getId() != null) {
        purchaseOrderLineRepository.remove(purchaseOrderLineRepository.find(lineToRemove.getId()));
      }
    }
  }

  protected List<? extends ShippableOrderLine> getShippableOrderLineList(
      ShippableOrder shippableOrder) {
    PurchaseOrder purchaseOrder = getPurchaseOrder(shippableOrder);
    if (purchaseOrder == null) {
      return null;
    }
    return purchaseOrder.getPurchaseOrderLineList();
  }

  protected PurchaseOrder getPurchaseOrder(ShippableOrder shippableOrder) {
    PurchaseOrder purchaseOrder = null;
    if (shippableOrder instanceof PurchaseOrder) {
      purchaseOrder = (PurchaseOrder) shippableOrder;
    }
    return purchaseOrder;
  }
}
