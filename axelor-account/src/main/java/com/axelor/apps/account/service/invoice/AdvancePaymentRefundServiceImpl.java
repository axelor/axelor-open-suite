package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdvancePaymentRefundServiceImpl implements AdvancePaymentRefundService {

  protected InvoiceRepository invoiceRepository;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoiceTermService invoiceTermService;
  protected InvoicePaymentToolService invoicePaymentToolService;

  @Inject
  public AdvancePaymentRefundServiceImpl(
      InvoiceRepository invoiceRepository,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoiceTermService invoiceTermService,
      InvoicePaymentToolService invoicePaymentToolService) {
    this.invoiceRepository = invoiceRepository;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoiceTermService = invoiceTermService;
    this.invoicePaymentToolService = invoicePaymentToolService;
  }

  @Override
  public void updateAdvancePaymentAmounts(Invoice refund) throws AxelorException {
    List<Invoice> advancePaymentList = getAdvancePaymentList(refund);

    if (!ObjectUtils.isEmpty(advancePaymentList)) {
      BigDecimal amountRemaining = refund.getAmountRemaining();

      for (Invoice advancePayment : advancePaymentList) {
        if (amountRemaining.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal paymentAmount = amountRemaining.min(advancePayment.getAmountRemaining());

          processInvoicePaymentImputation(paymentAmount, refund, advancePayment);
          amountRemaining = amountRemaining.subtract(paymentAmount);
        }
      }

      invoicePaymentToolService.updateAmountPaid(refund);
    }
  }

  protected List<Invoice> getAdvancePaymentList(Invoice refund) {
    List<Invoice> advancePaymentList = new ArrayList<>();

    if (refund == null) {
      return advancePaymentList;
    }

    if (refund.getOriginalInvoice() != null
        && refund.getOriginalInvoice().getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
        && refund.getOriginalInvoice().getStatusSelect() == InvoiceRepository.STATUS_VALIDATED) {
      advancePaymentList.add(refund.getOriginalInvoice());
    }

    String filter =
        "self.operationSubTypeSelect = ?1 AND self.originalInvoice = ?2 AND self.amountRemaining > 0 AND self.statusSelect = ?3";

    advancePaymentList.addAll(
        invoiceRepository
            .all()
            .filter(
                filter,
                InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE,
                refund,
                InvoiceRepository.STATUS_VALIDATED)
            .fetch());

    return advancePaymentList;
  }

  protected InvoicePayment createInvoicePaymentWithImputation(
      Invoice invoice, BigDecimal amount, int typeSelect) throws AxelorException {
    InvoicePayment imputationPayment =
        invoicePaymentCreateService.createInvoicePayment(
            invoice,
            amount,
            invoice.getDueDate(),
            invoice.getCurrency(),
            invoice.getPaymentMode(),
            typeSelect);
    imputationPayment.setCompanyBankDetails(invoice.getCompanyBankDetails());
    invoice.addInvoicePaymentListItem(imputationPayment);
    invoiceTermPaymentService.createInvoicePaymentTerms(imputationPayment, null);
    if (!ObjectUtils.isEmpty(imputationPayment.getInvoiceTermPaymentList())) {
      for (InvoiceTermPayment invoiceTermPayment : imputationPayment.getInvoiceTermPaymentList()) {
        invoiceTermService.updateInvoiceTermsPaidAmount(
            imputationPayment, invoiceTermPayment.getInvoiceTerm(), invoiceTermPayment);
      }
    }

    return imputationPayment;
  }

  @Transactional
  protected void processInvoicePaymentImputation(
      BigDecimal paymentAmount, Invoice refund, Invoice advancePayment) throws AxelorException {
    InvoicePayment refundPayment =
        createInvoicePaymentWithImputation(
            refund, paymentAmount, InvoicePaymentRepository.TYPE_ADV_PAYMENT_IMPUTATION);
    InvoicePayment invoicePayment =
        createInvoicePaymentWithImputation(
            advancePayment, paymentAmount, InvoicePaymentRepository.TYPE_REFUND_INVOICE);
    refundPayment.setDescription(advancePayment.getInvoiceId());
    refundPayment.setImputedBy(invoicePayment);
    refundPayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    invoicePayment.setImputedBy(refundPayment);
    invoicePayment.setDescription(refund.getInvoiceId());
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    invoicePaymentRepository.save(refundPayment);
    invoicePaymentRepository.save(invoicePayment);
    invoicePaymentToolService.updateAmountPaid(advancePayment);
  }
}
