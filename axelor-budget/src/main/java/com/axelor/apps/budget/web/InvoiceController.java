package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceService;
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
          && Beans.get(BudgetService.class).checkBudgetKeyInConfig(invoice.getCompany())) {
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

  public void computeInvoiceBudgetDistributionSumAmount(
      ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
      if (invoice != null && !CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
        BudgetInvoiceLineService budgetInvoiceLineService =
            Beans.get(BudgetInvoiceLineService.class);
        for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
          budgetInvoiceLineService.computeBudgetDistributionSumAmount(invoiceLine, invoice);
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
          response.setAlert(I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
