package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.reconcile.ForeignExchangeGapToolsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicePaymentForeignExchangeCreateServiceImpl
    implements InvoicePaymentForeignExchangeCreateService {

  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoiceTermService invoiceTermService;
  protected ForeignExchangeGapToolsService foreignExchangeGapToolsService;
  protected InvoicePaymentRepository invoicePaymentRepository;

  @Inject
  public InvoicePaymentForeignExchangeCreateServiceImpl(
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermService invoiceTermService,
      ForeignExchangeGapToolsService foreignExchangeGapToolsService,
      InvoicePaymentRepository invoicePaymentRepository) {
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoiceTermService = invoiceTermService;
    this.foreignExchangeGapToolsService = foreignExchangeGapToolsService;
    this.invoicePaymentRepository = invoicePaymentRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public InvoicePayment createForeignExchangeInvoicePayment(
      Reconcile newReconcile, Reconcile reconcile) throws AxelorException {
    Invoice invoice =
        reconcile.getDebitMoveLine().getMove().getInvoice() != null
            ? reconcile.getDebitMoveLine().getMove().getInvoice()
            : reconcile.getCreditMoveLine().getMove().getInvoice();
    InvoicePayment invoicePayment =
        invoicePaymentCreateService.createInvoicePayment(
            invoice,
            newReconcile.getAmount(),
            invoice.getDueDate(),
            newReconcile.getForeignExchangeMove().getCurrency(),
            invoice.getPaymentMode(),
            this.getInvoicePaymentType(reconcile));

    invoicePayment.setCompanyBankDetails(invoice.getCompanyBankDetails());
    invoicePayment.setMove(newReconcile.getForeignExchangeMove());
    invoice.addInvoicePaymentListItem(invoicePayment);
    invoiceTermPaymentService.createInvoicePaymentTerms(invoicePayment, null);
    if (!ObjectUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      for (InvoiceTermPayment invoiceTermPayment : invoicePayment.getInvoiceTermPaymentList()) {
        invoiceTermService.updateInvoiceTermsPaidAmount(
            invoicePayment, invoiceTermPayment.getInvoiceTerm(), invoiceTermPayment);
      }
    }

    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    invoicePaymentRepository.save(invoicePayment);

    return invoicePayment;
  }

  protected int getInvoicePaymentType(Reconcile reconcile) {
    boolean isDebit =
        foreignExchangeGapToolsService.isDebit(
            reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine());
    boolean isGain =
        foreignExchangeGapToolsService.isGain(
            reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine(), isDebit);

    return isGain
        ? InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN
        : InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS;
  }
}
