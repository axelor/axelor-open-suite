package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;

public interface InvoiceTermToolService {
  boolean isPartiallyPaid(InvoiceTerm invoiceTerm);
}
