package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceHRService {
  public String createDomainForBankCard(Invoice invoice);
}
