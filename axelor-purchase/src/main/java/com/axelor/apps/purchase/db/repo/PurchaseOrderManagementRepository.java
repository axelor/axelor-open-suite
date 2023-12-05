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
package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class PurchaseOrderManagementRepository extends PurchaseOrderRepository {

  @Override
  public PurchaseOrder copy(PurchaseOrder entity, boolean deep) {

    PurchaseOrder copy = super.copy(entity, deep);

    copy.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
    copy.setPurchaseOrderSeq(null);
    copy.setVersionNumber(1);
    copy.setEstimatedReceiptDate(null);
    copy.setValidatedByUser(null);
    copy.setValidationDateTime(null);
    copy.setOrderDate(Beans.get(AppBaseService.class).getTodayDate(entity.getCompany()));
    if (copy.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : copy.getPurchaseOrderLineList()) {
        purchaseOrderLine.setDesiredReceiptDate(null);
        purchaseOrderLine.setEstimatedReceiptDate(null);
      }
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
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
