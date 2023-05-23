package com.axelor.apps.budget.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.exception.IExceptionMessage;
import com.axelor.apps.budget.service.BudgetBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetBudgetService;
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
      PurchaseOrderBudgetBudgetService purchaseOrderBudgetBudgetService =
          Beans.get(PurchaseOrderBudgetBudgetService.class);
      if (purchaseOrder != null
          && purchaseOrder.getCompany() != null
          && Beans.get(BudgetBudgetService.class)
              .checkBudgetKeyInConfig(purchaseOrder.getCompany())) {
        if (!Beans.get(BudgetToolsService.class)
                .checkBudgetKeyAndRole(purchaseOrder.getCompany(), AuthUtils.getUser())
            && purchaseOrderBudgetBudgetService.isBudgetInLines(purchaseOrder)) {
          response.setInfo(
              I18n.get(IExceptionMessage.BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST));
          return;
        }
        String alertMessage =
            purchaseOrderBudgetBudgetService.computeBudgetDistribution(purchaseOrder);
        if (!Strings.isNullOrEmpty(alertMessage)) {
          response.setInfo(
              String.format(I18n.get(IExceptionMessage.BUDGET_KEY_NOT_FOUND), alertMessage));
        }
        response.setReload(true);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void computePurchaseOrderBudgetDistributionSumAmount(
      ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      if (purchaseOrder != null
          && !CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
        PurchaseOrderLineBudgetBudgetService purchaseOrderLineBudgetService =
            Beans.get(PurchaseOrderLineBudgetBudgetService.class);
        for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
          purchaseOrderLineBudgetService.computeBudgetDistributionSumAmount(
              purchaseOrderLine, purchaseOrder);
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
        PurchaseOrderLineBudgetBudgetService purchaseOrderLineBudgetBudgetService =
            Beans.get(PurchaseOrderLineBudgetBudgetService.class);
        boolean multiBudget =
            Beans.get(AppBudgetRepository.class).all().fetchOne().getManageMultiBudget();
        for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
          purchaseOrderLineBudgetBudgetService.fillBudgetStrOnLine(purchaseOrderLine, multiBudget);
        }
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void applyToAllBudgetDistribution(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderBudgetBudgetService purchaseOrderBudgetService =
          Beans.get(PurchaseOrderBudgetBudgetService.class);
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      AppBudget appBudget = Beans.get(AppBudgetRepository.class).all().fetchOne();

      if (purchaseOrder != null
          && purchaseOrder.getCompany() != null
          && Beans.get(BudgetBudgetService.class)
              .checkBudgetKeyInConfig(purchaseOrder.getCompany())) {
        if (!Beans.get(BudgetToolsService.class)
            .checkBudgetKeyAndRole(purchaseOrder.getCompany(), AuthUtils.getUser())) {
          response.setInfo(
              I18n.get(IExceptionMessage.BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST));
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
          response.setAlert(I18n.get(IExceptionMessage.NO_BUDGET_VALUES_FOUND));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
