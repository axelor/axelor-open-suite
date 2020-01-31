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
package com.axelor.apps.suppliermanagement.web;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.suppliermanagement.service.PurchaseOrderSupplierService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PurchaseOrderLineController {

  @Inject private PurchaseOrderLineRepository purchaseOrderLineRepo;

  @Inject private PurchaseOrderSupplierService purchaseOrderSupplierService;

  public void generateSuppliersRequests(ActionRequest request, ActionResponse response) {

    PurchaseOrderLine purchaseOrderLine =
        purchaseOrderLineRepo.find(request.getContext().asType(PurchaseOrderLine.class).getId());

    try {
      if (purchaseOrderLine.getPurchaseOrder() == null) {
        PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);

        purchaseOrderSupplierService.generateSuppliersRequests(purchaseOrderLine, purchaseOrder);
      } else {
        purchaseOrderSupplierService.generateSuppliersRequests(purchaseOrderLine);
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
