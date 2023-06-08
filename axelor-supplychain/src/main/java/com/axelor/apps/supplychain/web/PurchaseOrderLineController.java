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

import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineBudgetService;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.ContextTool;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class PurchaseOrderLineController {
  private final int startAxisPosition = 1;
  private final int endAxisPosition = 5;

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {
    PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

    if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
      purchaseOrderLine =
          Beans.get(PurchaseOrderLineServiceSupplychainImpl.class)
              .computeAnalyticDistribution(purchaseOrderLine);
      response.setValue("analyticMoveLineList", purchaseOrderLine.getAnalyticMoveLineList());
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

    Beans.get(PurchaseOrderLineBudgetService.class)
        .computeBudgetDistributionSumAmount(purchaseOrderLine, purchaseOrder);

    response.setValue(
        "budgetDistributionSumAmount", purchaseOrderLine.getBudgetDistributionSumAmount());
    response.setValue("budgetDistributionList", purchaseOrderLine.getBudgetDistributionList());
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextTool.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null) {
        return;
      }

      List<Long> analyticAccountList;
      AnalyticToolService analyticToolService = Beans.get(AnalyticToolService.class);
      AnalyticLineService analyticLineService = Beans.get(AnalyticLineService.class);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        if (analyticToolService.isPositionUnderAnalyticAxisSelect(purchaseOrder.getCompany(), i)) {
          analyticAccountList =
              analyticLineService.getAnalyticAccountIdList(purchaseOrder.getCompany(), i);

          if (ObjectUtils.isEmpty(analyticAccountList)) {
            response.setAttr(String.format("axis%dAnalyticAccount", i), "domain", "self.id IN (0)");
          } else {
            if (purchaseOrder.getCompany() != null) {
              String idList =
                  analyticAccountList.stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","));

              response.setAttr(
                  String.format("axis%dAnalyticAccount", i),
                  "domain",
                  String.format(
                      "self.id IN (%s) AND self.statusSelect = %d AND (self.company IS NULL OR self.company.id = %d)",
                      idList,
                      AnalyticAccountRepository.STATUS_ACTIVE,
                      purchaseOrder.getCompany().getId()));
            }
          }
        }
      }
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

      if (Beans.get(MoveLineComputeAnalyticService.class)
          .checkManageAnalytic(purchaseOrder.getCompany())) {
        purchaseOrderLine =
            Beans.get(PurchaseOrderLineServiceSupplyChain.class)
                .analyzePurchaseOrderLine(purchaseOrderLine, purchaseOrder.getCompany());
        response.setValue("analyticMoveLineList", purchaseOrderLine.getAnalyticMoveLineList());
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

      response.setAttrs(
          Beans.get(AnalyticLineService.class)
              .getAnalyticAxisAttrsMap(
                  purchaseOrder.getCompany(), startAxisPosition, endAxisPosition));
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
      Beans.get(PurchaseOrderLineServiceSupplyChain.class)
          .printAnalyticAccount(purchaseOrderLine, purchaseOrder.getCompany());

      response.setValue("axis1AnalyticAccount", purchaseOrderLine.getAxis1AnalyticAccount());
      response.setValue("axis2AnalyticAccount", purchaseOrderLine.getAxis2AnalyticAccount());
      response.setValue("axis3AnalyticAccount", purchaseOrderLine.getAxis3AnalyticAccount());
      response.setValue("axis4AnalyticAccount", purchaseOrderLine.getAxis4AnalyticAccount());
      response.setValue("axis5AnalyticAccount", purchaseOrderLine.getAxis5AnalyticAccount());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
