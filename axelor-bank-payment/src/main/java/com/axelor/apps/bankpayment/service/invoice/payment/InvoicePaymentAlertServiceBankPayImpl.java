package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentAlertServiceImpl;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import java.util.Optional;

public class InvoicePaymentAlertServiceBankPayImpl extends InvoicePaymentAlertServiceImpl {

  @Override
  public String validateBeforeReverse(InvoicePayment invoicePayment) {
    String alert = super.validateBeforeReverse(invoicePayment);

    if (BankOrderRepository.STATUS_CARRIED_OUT
        == Optional.ofNullable(invoicePayment)
            .map(InvoicePayment::getBankOrder)
            .map(BankOrder::getStatusSelect)
            .orElse(BankOrderRepository.STATUS_DRAFT)) {
      alert = BankPaymentExceptionMessage.INVOICE_PAYMENT_ALERT_BANK_ORDER_REVERSE;
    }

    return alert;
  }
}
