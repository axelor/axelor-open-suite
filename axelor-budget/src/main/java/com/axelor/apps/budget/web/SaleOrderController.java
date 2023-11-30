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

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.repo.AppBudgetRepository;
import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderController {

  public void computeBudgetDistribution(ActionRequest request, ActionResponse response) {

    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      SaleOrderBudgetService saleOrderBudgetService = Beans.get(SaleOrderBudgetService.class);
      if (saleOrder != null
          && saleOrder.getCompany() != null
          && Beans.get(BudgetService.class).checkBudgetKeyInConfig(saleOrder.getCompany())) {
        if (!Beans.get(BudgetToolsService.class)
                .checkBudgetKeyAndRole(saleOrder.getCompany(), AuthUtils.getUser())
            && saleOrderBudgetService.isBudgetInLines(saleOrder)) {
          response.setInfo(
              I18n.get(
                  BudgetExceptionMessage.BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST));
          return;
        }
        String alertMessage = saleOrderBudgetService.computeBudgetDistribution(saleOrder);
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

  public void computeSaleOrderBudgetDistributionSumAmount(
      ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
        SaleOrderLineBudgetServiceImpl saleOrderLineBudgetServiceImpl =
            Beans.get(SaleOrderLineBudgetServiceImpl.class);
        for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
          saleOrderLineBudgetServiceImpl.computeBudgetDistributionSumAmount(
              saleOrderLine, saleOrder);
        }
        response.setValue("saleOrderLineList", saleOrder.getSaleOrderLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillBudgetStrOnLine(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
        SaleOrderLineBudgetService saleOrderLineBudgetService =
            Beans.get(SaleOrderLineBudgetService.class);
        boolean multiBudget =
            Beans.get(AppBudgetRepository.class).all().fetchOne().getManageMultiBudget();
        for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
          saleOrderLineBudgetService.fillBudgetStrOnLine(saleOrderLine, multiBudget);
        }
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNoComputeBudget(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

      if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
        boolean isBudgetFilled = false;
        for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
          if (saleOrderLine.getBudget() != null
              || !CollectionUtils.isEmpty(saleOrderLine.getBudgetDistributionList())) {
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

  public void updateBudgetLines(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
        Beans.get(SaleOrderBudgetService.class).updateBudgetLinesFromSaleOrder(saleOrder);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateBudgetBalance(ActionRequest request, ActionResponse response) {

    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

      Beans.get(SaleOrderBudgetService.class).getBudgetExceedAlert(saleOrder);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.WARNING);
    }
  }
}
