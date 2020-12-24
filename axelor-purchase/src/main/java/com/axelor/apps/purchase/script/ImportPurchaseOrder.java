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
package com.axelor.apps.purchase.script;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Map;

@RequestScoped
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
      purchaseOrderService.requestPurchaseOrder(purchaseOrder);
    }

    return purchaseOrder;
  }
}
