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
package com.axelor.apps.purchase.script;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.google.inject.Inject;
import java.util.Map;

public class ImportPurchaseOrder {

  @Inject private SequenceService sequenceService;

  @Inject private PurchaseOrderService purchaseOrderService;

  public Object importPurchaseOrder(Object bean, Map<String, Object> values) throws Exception {
    assert bean instanceof PurchaseOrder;

    PurchaseOrder purchaseOrder = (PurchaseOrder) bean;

    purchaseOrder = purchaseOrderService.computePurchaseOrder(purchaseOrder);

    if (purchaseOrder.getStatusSelect() == 1) {
      purchaseOrder.setPurchaseOrderSeq(sequenceService.getDraftSequenceNumber(purchaseOrder));
    } else {
      // Setting the status to draft or else we can't request it
      purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
      purchaseOrderService.requestPurchaseOrder(purchaseOrder);
    }

    return purchaseOrder;
  }
}
