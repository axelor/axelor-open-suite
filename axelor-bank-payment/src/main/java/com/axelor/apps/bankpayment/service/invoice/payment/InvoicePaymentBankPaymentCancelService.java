package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.exception.AxelorException;

public interface InvoicePaymentBankPaymentCancelService {

  void cancelInvoicePayment(InvoicePayment invoicePayment) throws AxelorException;
}
