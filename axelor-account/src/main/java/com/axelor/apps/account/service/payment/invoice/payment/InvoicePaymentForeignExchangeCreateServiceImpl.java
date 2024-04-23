package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.reconcile.ForeignExchangeGapToolsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class InvoicePaymentForeignExchangeCreateServiceImpl
    implements InvoicePaymentForeignExchangeCreateService {

  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoiceTermService invoiceTermService;
  protected ForeignExchangeGapToolsService foreignExchangeGapToolsService;
  protected InvoiceTermToolService invoiceTermToolService;
  protected InvoicePaymentRepository invoicePaymentRepository;

  @Inject
  public InvoicePaymentForeignExchangeCreateServiceImpl(
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermService invoiceTermService,
      ForeignExchangeGapToolsService foreignExchangeGapToolsService,
      InvoiceTermToolService invoiceTermToolService,
      InvoicePaymentRepository invoicePaymentRepository) {
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoiceTermService = invoiceTermService;
    this.foreignExchangeGapToolsService = foreignExchangeGapToolsService;
    this.invoiceTermToolService = invoiceTermToolService;
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

    this.createInvoicePaymentTerms(invoicePayment);

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

  protected void createInvoicePaymentTerms(InvoicePayment invoicePayment) throws AxelorException {

    Invoice invoice = invoicePayment.getInvoice();
    if (invoice == null
        || CollectionUtils.isEmpty(invoicePayment.getInvoice().getInvoiceTermList())) {
      return;
    }

    List<InvoiceTerm> invoiceTerms =
        invoiceTermToolService.getPaymentVoucherInvoiceTerms(invoicePayment, invoice);

    if (CollectionUtils.isNotEmpty(invoiceTerms)) {
      this.initInvoiceTermPaymentsWithAmount(
          invoicePayment, invoiceTerms, invoicePayment.getAmount());
    }
  }

  protected void initInvoiceTermPaymentsWithAmount(
      InvoicePayment invoicePayment,
      List<InvoiceTerm> invoiceTermsToPay,
      BigDecimal availableAmount) {

    List<InvoiceTermPayment> invoiceTermPaymentList = new ArrayList<>();
    InvoiceTerm invoiceTermToPay;
    InvoiceTermPayment invoiceTermPayment;
    int invoiceTermCount = invoiceTermsToPay.size();

    if (invoicePayment != null) {
      invoicePayment.clearInvoiceTermPaymentList();
    }

    int i = 0;
    while (i < invoiceTermCount && availableAmount.signum() > 0) {
      invoiceTermToPay =
          invoiceTermToolService.getInvoiceTermToPay(
              invoicePayment, invoiceTermsToPay, availableAmount, i++, invoiceTermCount);

      BigDecimal invoiceTermCompanyAmount = invoiceTermToPay.getCompanyAmountRemaining();

      if (invoiceTermCompanyAmount.compareTo(availableAmount) >= 0) {
        invoiceTermPayment =
            invoiceTermPaymentService.createInvoiceTermPayment(
                invoicePayment, invoiceTermToPay, availableAmount);
        availableAmount = BigDecimal.ZERO;
      } else {
        invoiceTermPayment =
            invoiceTermPaymentService.createInvoiceTermPayment(
                invoicePayment, invoiceTermToPay, invoiceTermCompanyAmount);
        availableAmount = availableAmount.subtract(invoiceTermCompanyAmount);
      }

      invoiceTermPaymentList.add(invoiceTermPayment);

      if (invoicePayment != null) {
        invoicePayment.addInvoiceTermPaymentListItem(invoiceTermPayment);
      }
    }
  }
}
