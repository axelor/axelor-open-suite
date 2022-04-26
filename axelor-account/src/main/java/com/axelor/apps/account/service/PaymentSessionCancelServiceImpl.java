package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class PaymentSessionCancelServiceImpl implements PaymentSessionCancelService {
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;

  @Inject
  public PaymentSessionCancelServiceImpl(
      PaymentSessionRepository paymentSessionRepo, InvoiceTermRepository invoiceTermRepo) {
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelPaymentSession(PaymentSession paymentSession) {
    paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_CANCELLED);
    paymentSessionRepo.save(paymentSession);

    this.cancelInvoiceTerms(paymentSession);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void cancelInvoiceTerms(PaymentSession paymentSession) {
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo.all().filter("self.paymentSession = ?", paymentSession).order("id");

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, 0)).isEmpty()) {
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        this.cancelInvoiceTerm(invoiceTerm);

        invoiceTermRepo.save(invoiceTerm);
      }

      JPA.clear();
    }
  }

  protected void cancelInvoiceTerm(InvoiceTerm invoiceTerm) {
    invoiceTerm.setPaymentSession(null);
    invoiceTerm.setIsSelectedOnPaymentSession(false);
    invoiceTerm.setApplyFinancialDiscountOnPaymentSession(false);
    invoiceTerm.setPaymentAmount(BigDecimal.ZERO);
    invoiceTerm.setAmountPaid(BigDecimal.ZERO);
  }
}
