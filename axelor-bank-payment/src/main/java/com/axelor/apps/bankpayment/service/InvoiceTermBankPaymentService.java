package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;

public interface InvoiceTermBankPaymentService {

  BankOrderLineOrigin getAwaitingBankOrderLineOrigin(InvoiceTerm invoiceTerm);
}
