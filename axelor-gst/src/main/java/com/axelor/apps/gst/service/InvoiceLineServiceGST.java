package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Product;
import java.util.List;
import java.util.Map;

public interface InvoiceLineServiceGST {
  InvoiceLine calculateInvoiceLine(
      InvoiceLine invoiceLine, Boolean isSameState, Boolean isNullAddress);

  List<InvoiceLine> getInvoiceLineFromProduct(List<Product> productList);

//  Map<String, Object> fillProductInformationForInvoice(Invoice invoice, InvoiceLine invoiceLine);
}
