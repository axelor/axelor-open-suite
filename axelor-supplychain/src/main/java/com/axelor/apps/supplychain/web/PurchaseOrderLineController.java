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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.ContextTool;
import com.google.inject.Singleton;

@Singleton
public class PurchaseOrderLineController {

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {
    try {
      if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
        PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

        if (purchaseOrderLine.getPurchaseOrder() == null) {
          purchaseOrderLine.setPurchaseOrder(
              ContextTool.getContextParent(request.getContext(), PurchaseOrder.class, 1));
        }

        purchaseOrderLine =
            Beans.get(PurchaseOrderLineServiceSupplychainImpl.class)
                .computeAnalyticDistribution(purchaseOrderLine);
        response.setValue("analyticMoveLineList", purchaseOrderLine.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {
    PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

    purchaseOrderLine =
        Beans.get(PurchaseOrderLineServiceSupplyChain.class)
            .createAnalyticDistributionWithTemplate(purchaseOrderLine);
    response.setValue("analyticMoveLineList", purchaseOrderLine.getAnalyticMoveLineList());
  }

  public void computeBudgetDistributionSumAmount(ActionRequest request, ActionResponse response) {
    PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
    PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);

    Beans.get(PurchaseOrderLineServiceSupplychainImpl.class)
        .computeBudgetDistributionSumAmount(purchaseOrderLine, purchaseOrder);

    response.setValue(
        "budgetDistributionSumAmount", purchaseOrderLine.getBudgetDistributionSumAmount());
    response.setValue("budgetDistributionList", purchaseOrderLine.getBudgetDistributionList());
  }
}
