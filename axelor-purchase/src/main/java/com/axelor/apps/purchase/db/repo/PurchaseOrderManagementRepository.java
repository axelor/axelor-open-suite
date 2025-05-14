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
package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.PurchaseOrderSequenceService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class PurchaseOrderManagementRepository extends PurchaseOrderRepository {

  protected AppBaseService appBaseService;
  protected PurchaseOrderSequenceService purchaseOrderSequenceService;

  @Inject
  public PurchaseOrderManagementRepository(
      AppBaseService appBaseService, PurchaseOrderSequenceService purchaseOrderSequenceService) {
    this.appBaseService = appBaseService;
    this.purchaseOrderSequenceService = purchaseOrderSequenceService;
  }

  @Override
  public PurchaseOrder copy(PurchaseOrder entity, boolean deep) {

    PurchaseOrder copy = super.copy(entity, deep);

    copy.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
    copy.setPurchaseOrderSeq(null);
    copy.setVersionNumber(1);
    copy.setEstimatedReceiptDate(null);
    copy.setValidatedByUser(null);
    copy.setValidationDateTime(null);
    copy.setOrderDate(appBaseService.getTodayDate(entity.getCompany()));
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
      purchaseOrderSequenceService.setDraftSequence(purchaseOrder);
      return purchaseOrder;
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(PurchaseOrder purchaseOrder) {
    try {
      if (purchaseOrder.getStatusSelect() == PurchaseOrderRepository.STATUS_VALIDATED) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_CANNOT_DELETE_VALIDATED_ORDER));
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
    super.remove(purchaseOrder);
  }
}
