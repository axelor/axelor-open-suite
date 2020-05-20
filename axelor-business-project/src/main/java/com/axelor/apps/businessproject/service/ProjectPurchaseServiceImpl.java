/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.businessproject.module.BusinessProjectModule;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.util.List;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Alternative
@Priority(BusinessProjectModule.PRIORITY)
public class ProjectPurchaseServiceImpl extends SaleOrderPurchaseServiceImpl {

  @Inject
  public ProjectPurchaseServiceImpl(
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      PurchaseOrderLineServiceSupplyChain purchaseOrderLineServiceSupplychain,
      PurchaseOrderService purchaseOrderService) {
    super(
        purchaseOrderSupplychainService, purchaseOrderLineServiceSupplychain, purchaseOrderService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder createPurchaseOrder(
      Partner supplierPartner, List<SaleOrderLine> saleOrderLineList, SaleOrder saleOrder)
      throws AxelorException {
    PurchaseOrder purchaseOrder =
        super.createPurchaseOrder(supplierPartner, saleOrderLineList, saleOrder);

    if (purchaseOrder != null
        && saleOrder != null
        && Beans.get(AppBusinessProjectService.class).isApp("business-project")) {
      purchaseOrder.setProject(saleOrder.getProject());
    }

    return purchaseOrder;
  }
}
