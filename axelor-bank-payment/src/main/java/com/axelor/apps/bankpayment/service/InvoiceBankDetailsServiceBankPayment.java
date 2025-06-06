package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.BankDetails;

public interface InvoiceBankDetailsServiceBankPayment {
  BankDetails checkInvoiceBankDetails(Invoice invoice);
}
