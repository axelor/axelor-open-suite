/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.supplychain.service.PurchaseOrderCreateSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderMergingServiceSupplyChainImpl;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderMergingServiceProductionImpl
    extends PurchaseOrderMergingServiceSupplyChainImpl {

  protected ManufOrderRepository manufOrderRepository;

  @Inject
  public PurchaseOrderMergingServiceProductionImpl(
      AppPurchaseService appPurchaseService,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      PurchaseOrderRepository purchaseOrderRepository,
      DMSService dmsService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      PurchaseOrderCreateSupplychainService purchaseOrderCreateSupplychainService,
      ManufOrderRepository manufOrderRepository) {
    super(
        appPurchaseService,
        purchaseOrderService,
        purchaseOrderCreateService,
        purchaseOrderRepository,
        dmsService,
        purchaseOrderLineRepository,
        purchaseOrderCreateSupplychainService);
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  protected PurchaseOrder updateDatabase(
      PurchaseOrder purchaseOrderMerged, List<PurchaseOrder> purchaseOrdersToMerge) {
    List<ManufOrder> manufOrderList = this.getManufOrdersOfPurchaseOrders(purchaseOrdersToMerge);
    manufOrderList.forEach(ManufOrder::clearPurchaseOrderSet);
    purchaseOrderMerged = super.updateDatabase(purchaseOrderMerged, purchaseOrdersToMerge);

    for (ManufOrder manufOrder : manufOrderList) {
      manufOrder.addPurchaseOrderSetItem(purchaseOrderMerged);
    }
    return purchaseOrderMerged;
  }

  protected List<ManufOrder> getManufOrdersOfPurchaseOrders(List<PurchaseOrder> purchaseOrderList) {
    List<ManufOrder> manufOrderList = new ArrayList<>();
    for (PurchaseOrder purchaseOrder : purchaseOrderList) {
      manufOrderList.addAll(
          manufOrderRepository
              .all()
              .filter(":purchaseOrder MEMBER OF self.purchaseOrderSet")
              .bind("purchaseOrder", purchaseOrder)
              .fetch());
    }
    return manufOrderList;
  }
}
