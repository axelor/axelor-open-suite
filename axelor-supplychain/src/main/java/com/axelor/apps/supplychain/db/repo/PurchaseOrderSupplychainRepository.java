/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderManagementRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderSequenceService;
import com.axelor.apps.supplychain.service.PurchaseOrderAcknowledgmentService;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.inject.Beans;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderSupplychainRepository extends PurchaseOrderManagementRepository {

  @Inject
  public PurchaseOrderSupplychainRepository(
      AppBaseService appBaseService, PurchaseOrderSequenceService purchaseOrderSequenceService) {
    super(appBaseService, purchaseOrderSequenceService);
  }

  @Override
  public PurchaseOrder copy(PurchaseOrder entity, boolean deep) {

    PurchaseOrder copy = super.copy(entity, deep);

    if (!appBaseService.isApp("supplychain")) {
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
        purchaseOrderLine.setPurchaseOrderAcknowledgmentList(null);
      }
    }

    return copy;
  }

  @Override
  public PurchaseOrder save(PurchaseOrder purchaseOrder) {
    try {
      updateEstimatedReceiptDatesFromAcknowledgments(purchaseOrder);
      Beans.get(PurchaseOrderSupplychainService.class).checkAnalyticAxisByCompany(purchaseOrder);
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
    return super.save(purchaseOrder);
  }

  protected void updateEstimatedReceiptDatesFromAcknowledgments(PurchaseOrder purchaseOrder) {
    if (purchaseOrder == null) {
      return;
    }

    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
    if (CollectionUtils.isEmpty(purchaseOrderLineList)) {
      return;
    }

    PurchaseOrderAcknowledgmentService acknowledgmentService =
        Beans.get(PurchaseOrderAcknowledgmentService.class);

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
      if (purchaseOrderLine.getEstimatedReceiptDate() != null) {
        continue;
      }
      PurchaseOrderAcknowledgmentService.AcknowledgmentData acknowledgmentData =
          acknowledgmentService.computeAcknowledgmentData(purchaseOrderLine);
      LocalDate maxDeliveryDate = acknowledgmentData.maxDeliveryDate();
      if (maxDeliveryDate != null) {
        purchaseOrderLine.setEstimatedReceiptDate(maxDeliveryDate);
      }
    }
  }
}
