package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.InvoiceTerm;

public interface InvoiceTermDomainService {
  String createDomainForBankDetails(InvoiceTerm invoiceTerm);
}
