package com.axelor.apps.budget.service.invoice.workflow.ventilate;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.app.AppBudgetService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.workflow.ventilate.VentilateState;
import com.axelor.apps.account.service.invoice.workflow.ventilate.WorkflowVentilationService;
import com.axelor.apps.account.service.move.MoveCreateFromInvoiceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.budget.service.BudgetBudgetService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class VentilateStateBudget extends VentilateState {

  protected BudgetBudgetService budgetService;
  protected AppBudgetService appBudgetService;

  @Inject
  public VentilateStateBudget(
      SequenceService sequenceService,
      MoveCreateFromInvoiceService moveCreateFromInvoiceService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      InvoiceRepository invoiceRepo,
      WorkflowVentilationService workflowService,
      UserService userService,
      FixedAssetGenerationService fixedAssetService,
      InvoiceTermService invoiceTermService,
      AccountingSituationService accountingSituationService,
      BudgetBudgetService budgetService,
      AppBudgetService appBudgetService) {
    super(
        sequenceService,
        moveCreateFromInvoiceService,
        accountConfigService,
        appAccountService,
        invoiceRepo,
        workflowService,
        userService,
        fixedAssetService,
        invoiceTermService,
        accountingSituationService);
    this.budgetService = budgetService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public void process() throws AxelorException {
    super.process();
    if (appBudgetService.getAppBudget() != null) {
      budgetService.updateBudgetLinesFromInvoice(invoice);
    }
  }
}
