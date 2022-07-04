package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Company;

public interface InvoiceDomainService {
  String getPartnerBaseDomain(Company company, Invoice invoice, int invoiceTypeSelect);
}
