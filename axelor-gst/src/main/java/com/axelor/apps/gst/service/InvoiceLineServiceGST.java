package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.InvoiceLine;

public interface InvoiceLineServiceGST {
  InvoiceLine calculateInvoiceLine(
      InvoiceLine invoiceLine, Boolean isSameState, Boolean isNullAddress);
}
