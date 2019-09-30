/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class PurchaseOrderManagementRepository extends PurchaseOrderRepository {

  @Override
  public PurchaseOrder copy(PurchaseOrder entity, boolean deep) {

    PurchaseOrder copy = super.copy(entity, deep);

    copy.setStatusSelect(IPurchaseOrder.STATUS_DRAFT);
    copy.setPurchaseOrderSeq(null);
    copy.setVersionNumber(1);
    copy.setDeliveryDate(null);
    copy.setValidatedByUser(null);
    copy.setValidationDate(null);
    for (PurchaseOrderLine purchaseOrderLine : copy.getPurchaseOrderLineList()) {
      purchaseOrderLine.setDesiredDelivDate(null);
      purchaseOrderLine.setEstimatedDelivDate(null);
    }
    return copy;
  }

  @Override
  public PurchaseOrder save(PurchaseOrder purchaseOrder) {

    try {
      purchaseOrder = super.save(purchaseOrder);
      Beans.get(PurchaseOrderService.class).setDraftSequence(purchaseOrder);
      return purchaseOrder;
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
