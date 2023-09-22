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
package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.repo.AppBudgetRepository;
import com.axelor.utils.StringTool;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderLineController {

  public void getAccountDomain(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
    PurchaseOrder purchaseOrder;
    Company company = null;

    if (context.getParent() != null) {
      purchaseOrder = context.getParent().asType(PurchaseOrder.class);
      company = purchaseOrder.getCompany();
    }

    String domain = "self.id IN (0)";
    Set<Account> accountsSet =
        purchaseOrderLine.getBudget() != null
            ? purchaseOrderLine.getBudget().getAccountSet()
            : null;
    if (!CollectionUtils.isEmpty(accountsSet)) {
      domain = domain.replace("(0)", "(" + StringTool.getIdListString(accountsSet) + ")");
    }
    response.setAttr("account", "domain", domain);
  }

  public void setAccount(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
    if (context.getParent() != null) {
      Set<Account> accountsSet =
          purchaseOrderLine.getBudget() != null
              ? purchaseOrderLine.getBudget().getAccountSet()
              : null;
      response.setValue(
          "account", !CollectionUtils.isEmpty(accountsSet) ? accountsSet.iterator().next() : null);
    }
    if (!Beans.get(PurchaseOrderLineBudgetService.class)
        .addBudgetDistribution(purchaseOrderLine)
        .isEmpty()) {
      response.setValue(
          "budgetDistibutionList",
          Beans.get(PurchaseOrderLineBudgetService.class).addBudgetDistribution(purchaseOrderLine));
    }
  }

  public void validateBudgetLinesAmount(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      Beans.get(PurchaseOrderLineBudgetService.class)
          .checkAmountForPurchaseOrderLine(purchaseOrderLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  public void checkBudget(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      if (purchaseOrder != null && purchaseOrder.getCompany() != null) {
        response.setAttr(
            "budgetDistributionPanel",
            "readonly",
            !Beans.get(BudgetToolsService.class)
                    .checkBudgetKeyAndRole(purchaseOrder.getCompany(), AuthUtils.getUser())
                || purchaseOrder.getStatusSelect() == PurchaseOrderRepository.STATUS_CANCELED);
        response.setAttr(
            "budget",
            "readonly",
            !Beans.get(BudgetToolsService.class)
                    .checkBudgetKeyAndRole(purchaseOrder.getCompany(), AuthUtils.getUser())
                || purchaseOrder.getStatusSelect() == PurchaseOrderRepository.STATUS_CANCELED);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  public void setProductAccount(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);

      if (purchaseOrderLine.getProduct() == null) {
        response.setValue("account", null);
      } else {
        Account account =
            Beans.get(AccountManagementAccountService.class)
                .getProductAccount(
                    purchaseOrderLine.getProduct(),
                    purchaseOrder.getCompany(),
                    purchaseOrder.getFiscalPosition(),
                    true,
                    purchaseOrderLine.getFixedAssets());
        if (account.getCode().startsWith("2")
            || account.getCode().startsWith("4")
            || account.getCode().startsWith("6")) {
          response.setValue("account", account);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  public void fillBudgetStr(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      if (purchaseOrderLine != null) {
        PurchaseOrderLineBudgetService purchaseOrderLineBudgetService =
            Beans.get(PurchaseOrderLineBudgetService.class);
        boolean multiBudget =
            Beans.get(AppBudgetRepository.class).all().fetchOne().getManageMultiBudget();

        response.setValue(
            "budgetStr",
            purchaseOrderLineBudgetService.searchAndFillBudgetStr(purchaseOrderLine, multiBudget));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAccountDomain(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      String query =
          "self.accountType.technicalTypeSelect IN ('charge', 'income', 'immobilisation') AND self.statusSelect = 1";

      if (purchaseOrder != null && purchaseOrder.getCompany() != null) {
        query =
            query.concat(
                String.format(" AND self.company.id = %d", purchaseOrder.getCompany().getId()));
      }

      response.setAttr("account", "domain", query);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setGroupBudgetDomain(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      BudgetLevel global = (BudgetLevel) request.getContext().get("$global");
      String query = "self.id = 0";

      if (purchaseOrder != null && purchaseOrder.getCompany() != null) {
        query =
            Beans.get(PurchaseOrderLineBudgetService.class)
                .getGroupBudgetDomain(purchaseOrderLine, purchaseOrder, global);
      }

      response.setAttr("groupBudget", "domain", query);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setSectionBudgetDomain(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      BudgetLevel global = (BudgetLevel) request.getContext().get("$global");
      String query = "self.id = 0";

      if (purchaseOrder != null && purchaseOrder.getCompany() != null) {
        query =
            Beans.get(PurchaseOrderLineBudgetService.class)
                .getSectionBudgetDomain(purchaseOrderLine, purchaseOrder, global);
      }

      response.setAttr("section", "domain", query);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setLineBudgetDomain(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      BudgetLevel global = (BudgetLevel) request.getContext().get("$global");
      String query = "self.id = 0";

      if (purchaseOrder != null && purchaseOrder.getCompany() != null) {
        query =
            Beans.get(PurchaseOrderLineBudgetService.class)
                .getLineBudgetDomain(purchaseOrderLine, purchaseOrder, global, false);
      }

      response.setAttr("line", "domain", query);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setBudgetDomain(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
      BudgetLevel global = (BudgetLevel) request.getContext().get("$global");
      String query = "self.id = 0";

      if (purchaseOrder != null && purchaseOrder.getCompany() != null) {
        query =
            Beans.get(PurchaseOrderLineBudgetService.class)
                .getLineBudgetDomain(purchaseOrderLine, purchaseOrder, global, true);
      }

      response.setAttr("budget", "domain", query);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
