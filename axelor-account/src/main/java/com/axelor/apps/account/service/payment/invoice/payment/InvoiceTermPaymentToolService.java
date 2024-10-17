package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.meta.CallMethod;

public interface InvoiceTermPaymentToolService {

  @CallMethod
  boolean isPartialPayment(InvoicePayment invoicePayment);
}
