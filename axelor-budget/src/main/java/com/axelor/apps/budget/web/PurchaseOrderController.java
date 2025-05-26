/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.date.BudgetInitDateService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetService;
import com.axelor.apps.budget.web.tool.BudgetControllerTool;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppBudget;
import com.axelor.studio.db.repo.AppBudgetRepository;
import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderController {

  public void computeBudgetDistribution(ActionRequest request, ActionResponse response) {

    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      PurchaseOrderBudgetService purchaseOrderBudgetService =
          Beans.get(PurchaseOrderBudgetService.class);
      BudgetToolsService budgetToolsService = Beans.get(BudgetToolsService.class);
      if (purchaseOrder != null
          && purchaseOrder.getCompany() != null
          && budgetToolsService.checkBudgetKeyInConfig(purchaseOrder.getCompany())) {
        if (!budgetToolsService.checkBudgetKeyAndRole(
                purchaseOrder.getCompany(), AuthUtils.getUser())
            && purchaseOrderBudgetService.isBudgetInLines(purchaseOrder)) {
          response.setInfo(
              I18n.get(
                  BudgetExceptionMessage.BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST));
          return;
        }
        String alertMessage = purchaseOrderBudgetService.computeBudgetDistribution(purchaseOrder);
        if (!Strings.isNullOrEmpty(alertMessage)) {
          response.setInfo(
              String.format(I18n.get(BudgetExceptionMessage.BUDGET_KEY_NOT_FOUND), alertMessage));
        }
        response.setReload(true);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void computePurchaseOrderBudgetRemainingAmountToAllocate(
      ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      if (purchaseOrder != null
          && !CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
        BudgetToolsService budgetToolsService = Beans.get(BudgetToolsService.class);
        for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
          purchaseOrderLine.setBudgetRemainingAmountToAllocate(
              budgetToolsService.getBudgetRemainingAmountToAllocate(
                  purchaseOrderLine.getBudgetDistributionList(),
                  purchaseOrderLine.getCompanyExTaxTotal()));
        }
        response.setValue("purchaseOrderLineList", purchaseOrder.getPurchaseOrderLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillBudgetStrOnLine(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      if (purchaseOrder != null
          && !CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
        PurchaseOrderLineBudgetService purchaseOrderLineBudgetService =
            Beans.get(PurchaseOrderLineBudgetService.class);
        boolean multiBudget =
            Beans.get(AppBudgetRepository.class).all().fetchOne().getManageMultiBudget();
        for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
          purchaseOrderLineBudgetService.fillBudgetStrOnLine(purchaseOrderLine, multiBudget);
        }
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void applyToAllBudgetDistribution(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderBudgetService purchaseOrderBudgetService =
          Beans.get(PurchaseOrderBudgetService.class);
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      AppBudget appBudget = Beans.get(AppBudgetRepository.class).all().fetchOne();
      BudgetToolsService budgetToolsService = Beans.get(BudgetToolsService.class);
      if (purchaseOrder != null
          && purchaseOrder.getCompany() != null
          && budgetToolsService.checkBudgetKeyInConfig(purchaseOrder.getCompany())) {
        if (!budgetToolsService.checkBudgetKeyAndRole(
            purchaseOrder.getCompany(), AuthUtils.getUser())) {
          response.setInfo(
              I18n.get(
                  BudgetExceptionMessage.BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST));
          return;
        }
      }

      if (appBudget.getManageMultiBudget()) {
        purchaseOrderBudgetService.applyToallBudgetDistribution(purchaseOrder);
      } else {
        purchaseOrderBudgetService.setPurchaseOrderLineBudget(purchaseOrder);

        response.setValue("purchaseOrderLineList", purchaseOrder.getPurchaseOrderLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNoComputeBudget(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

      if (purchaseOrder != null
          && !CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
        boolean isBudgetFilled = false;
        for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
          if (purchaseOrderLine.getBudget() != null
              || !CollectionUtils.isEmpty(purchaseOrderLine.getBudgetDistributionList())) {
            isBudgetFilled = true;
          }
        }
        if (!isBudgetFilled) {
          Boolean isError = Beans.get(AppBudgetService.class).isMissingBudgetCheckError();
          if (isError != null) {
            if (isError) {
              response.setError(I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND_ERROR));
            } else {
              response.setAlert(I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND));
            }
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateBudgetDistributionAmountAvailable(
      ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      Beans.get(PurchaseOrderBudgetService.class)
          .updateBudgetDistributionAmountAvailable(purchaseOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void autoComputeBudgetDistribution(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    PurchaseOrderBudgetService purchaseOrderBudgetService =
        Beans.get(PurchaseOrderBudgetService.class);
    if (purchaseOrder != null
        && !CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())
        && !purchaseOrderBudgetService.isBudgetInLines(purchaseOrder)) {
      purchaseOrderBudgetService.autoComputeBudgetDistribution(purchaseOrder);
      response.setReload(true);
    }
  }

  public void validateRequest(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    PurchaseOrderBudgetService purchaseOrderBudgetService =
        Beans.get(PurchaseOrderBudgetService.class);
    if (purchaseOrder != null
        && !CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      if (purchaseOrderBudgetService.isBudgetInLines(purchaseOrder)) {
        String budgetExceedAlert = purchaseOrderBudgetService.getBudgetExceedAlert(purchaseOrder);
        BudgetControllerTool.verifyBudgetExceed(budgetExceedAlert, true, response);
      } else {
        BudgetControllerTool.verifyMissingBudget(response);
      }
    }
  }

  public void initializeBudgetDates(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    Beans.get(BudgetInitDateService.class).initializeBudgetDates(purchaseOrder);

    response.setValue("purchaseOrderLineList", purchaseOrder.getPurchaseOrderLineList());
  }
}
