/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.web;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTree;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineTreeRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineCalculationComboService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTreeService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;

@Singleton
public class SaleOrderLineTreeController {

  public void fillFields(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLineTree saleOrderLineTree = context.asType(SaleOrderLineTree.class);
    saleOrderLineTree = Beans.get(SaleOrderLineTreeService.class).fillFields(saleOrderLineTree);
    response.setValues(saleOrderLineTree);
  }

  public void updateFields(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLineTree saleOrderLineTree = context.asType(SaleOrderLineTree.class);
    saleOrderLineTree = Beans.get(SaleOrderLineTreeService.class).updateFields(saleOrderLineTree);
    response.setValues(saleOrderLineTree);
  }

  public void updateUnitPrice(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLineTree saleOrderLineTree = context.asType(SaleOrderLineTree.class);
    saleOrderLineTree =
        Beans.get(SaleOrderLineTreeService.class).updateUnitPrice(saleOrderLineTree);
    response.setValues(saleOrderLineTree);
  }

  public void computePrices(ActionRequest request, ActionResponse response) throws AxelorException {

    Long id = (Long) request.getContext().get("id");
    SaleOrderLine saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(id);

    Beans.get(SaleOrderLineCalculationComboService.class)
        .computePriceAndRelatedFields(saleOrderLine);

    response.setValue("price", saleOrderLine.getPrice());
    response.setValue("subTotalCostPrice", saleOrderLine.getSubTotalCostPrice());

    response.setReload(true);
  }

  public void hasSubElement(ActionRequest request, ActionResponse response) {

    Long id = (Long) request.getContext().get("id");
    SaleOrderLine saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(id);
    saleOrderLine = Beans.get(SaleOrderLineTreeService.class).hasSubElement(saleOrderLine);

    response.setValue("hasTree", saleOrderLine.getHasTree());
    response.setReload(true);
  }

  public void saveHasSubElement(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Long id = (Long) request.getContext().get("id");
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(id);
    saleOrder = Beans.get(SaleOrderLineTreeService.class).saveHasSubElement(saleOrder);
    response.setValues(saleOrder);
    response.setReload(true);
  }

  public void removeSubElement(ActionRequest request, ActionResponse response)
      throws AxelorAlertException {
    try {
      Long id = (Long) request.getContext().get("id");
      SaleOrderLineTree saleOrderLineTree = Beans.get(SaleOrderLineTreeRepository.class).find(id);
      Beans.get(SaleOrderLineTreeService.class).removeElement(saleOrderLineTree);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.WARNING);
    }
  }

  public void reloadView(ActionRequest request, ActionResponse response)
      throws InterruptedException {

    if (request.getContext().get("id") == null) {
      // have to do this trick to avoid an error in view, will be fixed in future AOP release.
      // if we do not wait here, in the next action we will not have the id.
      response.setReload(true);
      Thread.sleep(400);
    }
  }
}
