package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import java.util.Optional;

public class InvoicePaymentAlertServiceImpl implements InvoicePaymentAlertService {

  @Override
  public String validateBeforeReverse(InvoicePayment invoicePayment) {
    if (invoicePayment == null
        || invoicePayment.getStatusSelect() == InvoicePaymentRepository.STATUS_CANCELED) {
      return "";
    }

    String alert = AccountExceptionMessage.INVOICE_PAYMENT_ALERT_DEFAULT_REVERSE;

    DepositSlip depositSlip =
        Optional.of(invoicePayment)
            .map(InvoicePayment::getMove)
            .map(Move::getPaymentVoucher)
            .map(PaymentVoucher::getDepositSlip)
            .orElse(null);
    if (depositSlip != null && depositSlip.getPublicationDate() != null) {
      alert = AccountExceptionMessage.INVOICE_PAYMENT_ALERT_VOUCHER_DEPOSIT_REVERSE;
    }

    return alert;
  }
}
