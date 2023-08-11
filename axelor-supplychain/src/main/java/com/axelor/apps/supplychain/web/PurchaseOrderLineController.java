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
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.ContextTool;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class PurchaseOrderLineController {

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

      if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
        AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine);

        Beans.get(AnalyticLineModelService.class).computeAnalyticDistribution(analyticLineModel);

        response.setValue(
            "analyticDistributionTemplate", analyticLineModel.getAnalyticDistributionTemplate());
        response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine);

      Beans.get(AnalyticLineModelService.class)
          .createAnalyticDistributionWithTemplate(analyticLineModel);

      response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder =
          ContextTool.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null) {
        return;
      }

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine);
      // TODO uncomment after merge #66647
      // response.setAttrs(Beans.get(AnalyticGroupService.class).getAnalyticAxisDomainAttrsMap(analyticLineModel, purchaseOrder.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextTool.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null) {
        return;
      }

      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine);

      if (Beans.get(AnalyticLineModelService.class)
          .analyzeAnalyticLineModel(analyticLineModel, purchaseOrder.getCompany())) {
        response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextTool.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null || purchaseOrder.getCompany() == null) {
        return;
      }

      Map<String, Map<String, Object>> attrsMap = new HashMap<>();
      // TODO uncomment after merge #66647
      // Beans.get(AnalyticAttrsService.class).addAnalyticAxisAttrs(purchaseOrder.getCompany(),
      // attrsMap);

      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextTool.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null || purchaseOrder.getCompany() == null) {
        return;
      }

      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine);

      // TODO uncomment after merge #66647
      // response.setValues(Beans.get(AnalyticGroupService.class).getAnalyticAccountValueMap(analyticLineModel, purchaseOrder.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
