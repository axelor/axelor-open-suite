package com.axelor.apps.account.service.sublines;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.rpc.Context;

public interface InvoiceSubLineService {
  void updateRelatedInvoiceLinesOnPriceChange(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException;

  void updateRelatedInvoiceLinesOnQtyChange(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException;

  void updateRelatedOrderLines(Invoice invoice) throws AxelorException;

  InvoiceLine setLineIndex(InvoiceLine invoiceLine, Context context);

  InvoiceLine updateOnInvoiceLineListChange(InvoiceLine invoiceLine);

  Invoice getParentInvoiceLine(Context context);

  String getProductDomain(Invoice invoice, boolean isFilterOnSupplier);
}
