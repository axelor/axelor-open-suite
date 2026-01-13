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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.date.BudgetInitDateService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceService;
import com.axelor.apps.budget.web.tool.BudgetControllerTool;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceController {

  public void computeBudgetDistribution(ActionRequest request, ActionResponse response) {

    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
      BudgetInvoiceService budgetInvoiceService = Beans.get(BudgetInvoiceService.class);
      if (invoice != null
          && Beans.get(BudgetToolsService.class).checkBudgetKeyInConfig(invoice.getCompany())) {
        if (invoice.getCompany() != null
            && !Beans.get(BudgetToolsService.class)
                .checkBudgetKeyAndRole(invoice.getCompany(), AuthUtils.getUser())
            && budgetInvoiceService.isBudgetInLines(invoice)) {
          response.setInfo(
              I18n.get(
                  BudgetExceptionMessage.BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST));
          return;
        }
        String alertMessage = budgetInvoiceService.computeBudgetDistribution(invoice);
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

  public void computeInvoiceBudgetRemainingAmountToAllocate(
      ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
      if (invoice != null && !CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
        BudgetToolsService budgetToolsService = Beans.get(BudgetToolsService.class);
        for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
          invoiceLine.setBudgetRemainingAmountToAllocate(
              budgetToolsService.getBudgetRemainingAmountToAllocate(
                  invoiceLine.getBudgetDistributionList(), invoiceLine.getCompanyExTaxTotal()));
        }
        response.setValue("invoiceLineList", invoice.getInvoiceLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNoComputeBudget(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);

      if (invoice != null && !CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
        boolean isBudgetFilled = false;
        for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
          if (invoiceLine.getBudget() != null
              || !CollectionUtils.isEmpty(invoiceLine.getBudgetDistributionList())) {
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

  @ErrorException
  public void autoComputeBudgetDistribution(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);
    BudgetInvoiceService budgetInvoiceService = Beans.get(BudgetInvoiceService.class);
    if (invoice != null
        && !CollectionUtils.isEmpty(invoice.getInvoiceLineList())
        && !budgetInvoiceService.isBudgetInLines(invoice)) {
      budgetInvoiceService.autoComputeBudgetDistribution(invoice);
      response.setValue("invoiceLineList", invoice.getInvoiceLineList());
    }
  }

  public void validateVentilation(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    BudgetInvoiceService budgetInvoiceService = Beans.get(BudgetInvoiceService.class);
    if (invoice != null && !CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      if (budgetInvoiceService.isBudgetInLines(invoice)) {
        String budgetExceedAlert = budgetInvoiceService.getBudgetExceedAlert(invoice);
        BudgetControllerTool.verifyBudgetExceed(budgetExceedAlert, false, response);
      } else {
        BudgetControllerTool.verifyMissingBudget(response);
      }
    }
  }

  public void initializeBudgetDates(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);
    Beans.get(BudgetInitDateService.class).initializeBudgetDates(invoice);

    response.setValue("invoiceLineList", invoice.getInvoiceLineList());
  }
}
