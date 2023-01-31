package com.axelor.apps.account.service.invoice.print;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;

public interface InvoiceProductStatementService {
  String getInvoiceProductStatement(Invoice invoice) throws AxelorException;
}
