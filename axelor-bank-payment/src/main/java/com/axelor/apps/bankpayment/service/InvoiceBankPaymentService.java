package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;

public interface InvoiceBankPaymentService {
  void cancelBillOfExchange(Invoice invoice) throws AxelorException;
}
