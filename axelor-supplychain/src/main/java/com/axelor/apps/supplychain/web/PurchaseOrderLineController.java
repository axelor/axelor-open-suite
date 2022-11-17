/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.ArrayList;
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

    Beans.get(PurchaseOrderLineServiceSupplychainImpl.class)
        .computeBudgetDistributionSumAmount(purchaseOrderLine, purchaseOrder);

    response.setValue(
        "budgetDistributionSumAmount", purchaseOrderLine.getBudgetDistributionSumAmount());
    response.setValue("budgetDistributionList", purchaseOrderLine.getBudgetDistributionList());
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = null;
      if (request.getContext().getParent() != null
          && (PurchaseOrder.class).equals(request.getContext().getParent().getContextClass())) {
        purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      }

      AnalyticToolService analyticToolService = Beans.get(AnalyticToolService.class);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        List<Long> analyticAccountList = new ArrayList<>();
        if (purchaseOrder != null
            && analyticToolService.isPositionUnderAnalyticAxisSelect(
                purchaseOrder.getCompany(), i)) {

          AnalyticAxis analyticAxis = new AnalyticAxis();

          for (AnalyticAxisByCompany axis :
              Beans.get(AccountConfigService.class)
                  .getAccountConfig(purchaseOrder.getCompany())
                  .getAnalyticAxisByCompanyList()) {
            if (axis.getSequence() + 1 == i) {
              analyticAxis = axis.getAnalyticAxis();
            }
          }

          for (AnalyticAccount analyticAccount :
              Beans.get(AnalyticAccountRepository.class).findByAnalyticAxis(analyticAxis).fetch()) {
            analyticAccountList.add(analyticAccount.getId());
          }

          if (ObjectUtils.isEmpty(analyticAccountList)) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                "domain",
                "self.id IN (0)");
          } else {
            if (purchaseOrder.getCompany() != null) {
              String idList =
                  analyticAccountList.stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","));

              response.setAttr(
                  "axis" + i + "AnalyticAccount",
                  "domain",
                  "self.id IN ("
                      + idList
                      + ") AND self.statusSelect = "
                      + AnalyticAccountRepository.STATUS_ACTIVE
                      + " AND (self.company is null OR self.company.id = "
                      + purchaseOrder.getCompany().getId()
                      + ")");
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
      if (request.getContext().getParent() != null
          && (PurchaseOrder.class).equals(request.getContext().getParent().getContextClass())) {

        PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
        PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
        if (purchaseOrder != null
            && Beans.get(MoveLineComputeAnalyticService.class)
                .checkManageAnalytic(purchaseOrder.getCompany())) {
          purchaseOrderLine =
              Beans.get(PurchaseOrderLineServiceSupplyChain.class)
                  .analyzePurchaseOrderLine(
                      purchaseOrderLine, purchaseOrder, purchaseOrder.getCompany());
          response.setValue("analyticMoveLineList", purchaseOrderLine.getAnalyticMoveLineList());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = null;

      if (request.getContext().getParent() != null
          && (PurchaseOrder.class).equals(request.getContext().getParent().getContextClass())) {
        purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      }

      if (purchaseOrder != null && purchaseOrder.getCompany() != null) {
        AccountConfig accountConfig =
            Beans.get(AccountConfigService.class).getAccountConfig(purchaseOrder.getCompany());
        if (Beans.get(MoveLineComputeAnalyticService.class)
            .checkManageAnalytic(purchaseOrder.getCompany())) {
          AnalyticAxis analyticAxis = null;
          for (int i = startAxisPosition; i <= endAxisPosition; i++) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                "hidden",
                !(i <= accountConfig.getNbrOfAnalyticAxisSelect()));
            for (AnalyticAxisByCompany analyticAxisByCompany :
                accountConfig.getAnalyticAxisByCompanyList()) {
              if (analyticAxisByCompany.getSequence() + 1 == i) {
                analyticAxis = analyticAxisByCompany.getAnalyticAxis();
              }
            }
            if (analyticAxis != null) {
              response.setAttr(
                  "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                  "title",
                  analyticAxis.getName());
              analyticAxis = null;
            }
          }
        } else {
          response.setAttr("analyticDistributionTemplate", "hidden", true);
          response.setAttr("analyticMoveLineList", "hidden", true);
          for (int i = startAxisPosition; i <= endAxisPosition; i++) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"), "hidden", true);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = null;
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      if (request.getContext().getParent() != null
          && (PurchaseOrder.class).equals(request.getContext().getParent().getContextClass())) {
        purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      }
      if (purchaseOrderLine != null && purchaseOrder != null) {
        Beans.get(PurchaseOrderLineServiceSupplyChain.class)
            .printAnalyticAccount(purchaseOrderLine, purchaseOrder.getCompany());
        response.setValues(purchaseOrderLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
