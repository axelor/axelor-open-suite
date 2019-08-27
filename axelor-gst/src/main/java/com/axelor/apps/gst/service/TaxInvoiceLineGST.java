package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.service.invoice.generator.tax.TaxInvoiceLine;
import java.util.List;

public class TaxInvoiceLineGST extends TaxInvoiceLine {

  public TaxInvoiceLineGST(Invoice invoice, List<InvoiceLine> invoiceLines) {
    super(invoice, invoiceLines);
    System.out.println("CALL CONTRUCTOR IN TAX INVOICE LINE");
  }

  @Override
  public List<InvoiceLineTax> creates() {

    System.err.println("call method which we extends..");
    return null;
  }
}
