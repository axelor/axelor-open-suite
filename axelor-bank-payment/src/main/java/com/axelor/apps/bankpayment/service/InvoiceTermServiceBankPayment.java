package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.base.db.BankDetails;

public interface InvoiceTermServiceBankPayment {
  BankDetails checkInvoiceTermBankDetails(InvoiceTerm invoiceTerm);
}
