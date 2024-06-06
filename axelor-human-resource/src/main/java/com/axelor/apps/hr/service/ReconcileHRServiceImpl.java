package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermPaymentRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.ReconcileSequenceService;
import com.axelor.apps.account.service.ReconcileServiceImpl;
import com.axelor.apps.account.service.SubrogationReleaseWorkflowService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveAdjustementService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.PaymentMoveLineDistributionService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;

public class ReconcileHRServiceImpl extends ReconcileServiceImpl {

  @Inject
  public ReconcileHRServiceImpl(
      MoveToolService moveToolService,
      AccountCustomerService accountCustomerService,
      AccountConfigService accountConfigService,
      ReconcileRepository reconcileRepository,
      MoveAdjustementService moveAdjustementService,
      ReconcileSequenceService reconcileSequenceService,
      InvoicePaymentCancelService invoicePaymentCancelService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      MoveLineTaxService moveLineTaxService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoiceTermService invoiceTermService,
      AppBaseService appBaseService,
      PaymentMoveLineDistributionService paymentMoveLineDistributionService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermPaymentRepository invoiceTermPaymentRepo,
      InvoicePaymentToolService invoicePaymentToolService,
      MoveLineControlService moveLineControlService,
      MoveLineRepository moveLineRepo,
      SubrogationReleaseWorkflowService subrogationReleaseWorkflowService,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveValidateService moveValidateService,
      CurrencyScaleService currencyScaleService,
      InvoiceTermPfpService invoiceTermPfpService,
      CurrencyService currencyService) {
    super(
        moveToolService,
        accountCustomerService,
        accountConfigService,
        reconcileRepository,
        moveAdjustementService,
        reconcileSequenceService,
        invoicePaymentCancelService,
        invoicePaymentCreateService,
        moveLineTaxService,
        invoicePaymentRepo,
        invoiceTermService,
        appBaseService,
        paymentMoveLineDistributionService,
        invoiceTermPaymentService,
        invoiceTermPaymentRepo,
        invoicePaymentToolService,
        moveLineControlService,
        moveLineRepo,
        subrogationReleaseWorkflowService,
        moveCreateService,
        moveLineCreateService,
        moveValidateService,
        currencyScaleService,
        invoiceTermPfpService,
        currencyService);
  }

  @Override
  protected boolean isAccountTypeTax(MoveLine it) {
    return (it.getMove().getExpense() == null && it.getTaxLine() == null)
        && it.getAccount()
            .getAccountType()
            .getTechnicalTypeSelect()
            .equals(AccountTypeRepository.TYPE_TAX)
        && it.getAccount().getIsTaxAuthorizedOnMoveLine();
  }
}
