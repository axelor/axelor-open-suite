/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderManagementRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class PurchaseOrderSupplychainRepository extends PurchaseOrderManagementRepository {

  @Inject private AppService appService;

  @Override
  public PurchaseOrder copy(PurchaseOrder entity, boolean deep) {

    PurchaseOrder copy = super.copy(entity, deep);

    if (!appService.isApp("supplychain")) {
      return copy;
    }

    copy.setReceiptState(PurchaseOrderRepository.STATE_NOT_RECEIVED);
    copy.setAmountInvoiced(null);

    if (copy.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : copy.getPurchaseOrderLineList()) {
        purchaseOrderLine.setReceiptState(null);
        purchaseOrderLine.setReceivedQty(null);
        purchaseOrderLine.setAmountInvoiced(null);
        purchaseOrderLine.setInvoiced(null);
      }
    }

    return copy;
  }

  @Override
  public PurchaseOrder save(PurchaseOrder purchaseOrder) {

    if (appService.isApp("supplychain")) {
      Beans.get(PurchaseOrderSupplychainService.class).generateBudgetDistribution(purchaseOrder);
    }
    return super.save(purchaseOrder);
  }
}
