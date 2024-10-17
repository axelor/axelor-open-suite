package com.axelor.apps.account.service.invoice.tax;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceTaxComputeService {

  void recomputeInvoiceTaxAmounts(Invoice invoice);
}
