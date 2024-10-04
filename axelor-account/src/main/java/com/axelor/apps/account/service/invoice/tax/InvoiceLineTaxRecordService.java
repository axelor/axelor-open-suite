package com.axelor.apps.account.service.invoice.tax;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.base.AxelorException;

public interface InvoiceLineTaxRecordService {
  void recomputeAmounts(InvoiceLineTax invoiceLineTax, Invoice invoice) throws AxelorException;
}
