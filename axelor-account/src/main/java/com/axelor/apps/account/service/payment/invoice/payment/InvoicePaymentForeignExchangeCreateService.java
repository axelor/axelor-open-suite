package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;

public interface InvoicePaymentForeignExchangeCreateService {

  InvoicePayment createForeignExchangeInvoicePayment(Reconcile newReconcile, Reconcile reconcile)
      throws AxelorException;
}
