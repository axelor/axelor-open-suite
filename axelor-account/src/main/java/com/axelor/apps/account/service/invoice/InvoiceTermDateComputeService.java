package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceTermDateComputeService {
  void fillWithInvoiceDueDate(Invoice invoice);
}
