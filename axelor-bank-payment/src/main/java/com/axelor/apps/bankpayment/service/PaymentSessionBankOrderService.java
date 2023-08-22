package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface PaymentSessionBankOrderService {
  @Transactional(rollbackOn = {Exception.class})
  BankOrder generateBankOrderFromPaymentSession(PaymentSession paymentSession)
      throws AxelorException;

  void createOrUpdateBankOrderLineFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrder bankOrder,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException;

  @Transactional
  void manageInvoicePayment(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, BigDecimal reconciliedAmount);
}
