package com.axelor.apps.account.service.invoice.print;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceProductStatementService {
  String getInvoiceProductStatement(Invoice invoice);
}
