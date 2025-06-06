package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.BankDetails;

public interface InvoiceBankDetailsService {
  BankDetails getDefaultBankDetails(Invoice invoice);
}
