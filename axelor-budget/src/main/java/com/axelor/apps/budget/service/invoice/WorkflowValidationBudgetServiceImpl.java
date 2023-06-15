package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.service.WorkflowValidationServiceProjectImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class WorkflowValidationBudgetServiceImpl extends WorkflowValidationServiceProjectImpl {

  protected AppBudgetService appBudgetService;
  protected BudgetInvoiceService budgetInvoiceService;

  @Inject
  public WorkflowValidationBudgetServiceImpl(
      InvoicingProjectRepository invoicingProjectRepo,
      AppBudgetService appBudgetService,
      BudgetInvoiceService budgetInvoiceService) {
    super(invoicingProjectRepo);
    this.appBudgetService = appBudgetService;
    this.budgetInvoiceService = budgetInvoiceService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void afterValidation(Invoice invoice) throws AxelorException {
    super.afterValidation(invoice);

    if ((invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)) {
      if (appBudgetService.getAppBudget() != null
          && appBudgetService.getAppBudget().getManageMultiBudget()) {
        budgetInvoiceService.generateBudgetDistribution(invoice);
      }
    }
  }
}
