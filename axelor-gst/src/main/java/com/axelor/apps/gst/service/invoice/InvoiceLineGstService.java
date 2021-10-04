package com.axelor.apps.gst.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;

public interface InvoiceLineGstService {

  public Boolean checkIsStateDiff(Invoice invoice);

  public InvoiceLine calculateInvoiceLine(InvoiceLine invoiceline, Boolean isStateDiff);
}
