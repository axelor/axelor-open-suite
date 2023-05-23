package com.axelor.apps.budget.service.invoice.workflow.ventilate;

import com.axelor.apps.account.service.invoice.workflow.cancel.WorkflowCancelService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.BudgetInvoiceService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class CancelStateBudget /*extends CancelState*/ {

  protected BudgetInvoiceService budgetInvoiceService;
  private WorkflowCancelService workflowService;

  @Inject
  public CancelStateBudget(
      WorkflowCancelService workflowService, BudgetInvoiceService budgetInvoiceService) {
    this.workflowService = workflowService;
    this.budgetInvoiceService = budgetInvoiceService;
  }

  protected void updateInvoiceFromCancellation() throws AxelorException {
    /*setStatus();
    if (Beans.get(AccountConfigService.class)
        .getAccountConfig(invoice.getCompany())
        .getIsManagePassedForPayment()) {
      setPfpStatus();
    }

    budgetInvoiceService.updateBudgetLinesFromInvoice(invoice);

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      invoiceLine.clearBudgetDistributionList();
    }*/
  }
}
