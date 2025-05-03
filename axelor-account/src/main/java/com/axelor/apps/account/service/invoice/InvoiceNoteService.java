package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceNoteService {
  void generateInvoiceNote(Invoice invoice);
}
