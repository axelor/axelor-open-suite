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
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.suppliermanagement.exceptions.IExceptionMessage;
import com.axelor.apps.suppliermanagement.service.PurchaseOrderSupplierService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PurchaseOrderController {

  public void generateSuppliersPurchaseOrder(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    try {
      Beans.get(PurchaseOrderSupplierService.class)
          .generateSuppliersPurchaseOrder(
              Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId()));
      response.setFlash(I18n.get(IExceptionMessage.PURCHASE_ORDER_1));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateSuppliersRequests(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    try {
      Beans.get(PurchaseOrderSupplierService.class)
          .generateAllSuppliersRequests(
              Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId()));
      response.setFlash(I18n.get(IExceptionMessage.PURCHASE_ORDER_2));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
