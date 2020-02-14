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
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ProjectPurchaseServiceImpl extends SaleOrderPurchaseServiceImpl {

  @Inject
  public ProjectPurchaseServiceImpl(
      PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl,
      PurchaseOrderLineServiceSupplychainImpl purchaseOrderLineServiceSupplychainImpl) {
    super(purchaseOrderServiceSupplychainImpl, purchaseOrderLineServiceSupplychainImpl);
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
