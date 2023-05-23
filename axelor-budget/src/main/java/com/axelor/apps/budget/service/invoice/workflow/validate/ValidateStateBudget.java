package com.axelor.apps.budget.service.invoice.workflow.ventilate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.app.AppBudgetService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.workflow.validate.ValidateState;
import com.axelor.apps.account.service.invoice.workflow.validate.WorkflowValidationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.budget.service.BudgetInvoiceService;
import com.google.inject.Inject;

public class ValidateStateBudget extends ValidateState {

  protected BudgetInvoiceService budgetInvoiceService;
  protected AppBudgetService appBudgetService;

  @Inject
  public ValidateStateBudget(
      UserService userService,
      BlockingService blockingService,
      WorkflowValidationService workflowValidationService,
      AppBaseService appBaseService,
      InvoiceService invoiceService,
      AppAccountService appAccountService,
      AccountingSituationService accountingSituationService,
      BudgetInvoiceService budgetInvoiceService,
      AppBudgetService appBudgetService) {
    super(
        userService,
        blockingService,
        workflowValidationService,
        appBaseService,
        invoiceService,
        appAccountService,
        accountingSituationService);
    this.budgetInvoiceService = budgetInvoiceService;
    this.appBudgetService = appBudgetService;
  }

  public void init(Invoice invoice) {
    this.invoice = invoice;
  }

  @Override
  public void process() throws AxelorException {
    if ((invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)) {
      if (appBudgetService.getAppBudget() != null
          && appBudgetService.getAppBudget().getManageMultiBudget()) {
        budgetInvoiceService.generateBudgetDistribution(invoice);
      }
      budgetInvoiceService.updateBudgetLinesFromInvoice(invoice);
    }
    super.process();
  }
}
