package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;

public interface InvoicePaymentAlertService {
  String validateBeforeReverse(InvoicePayment invoicePayment);
}
