package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;

public interface InvoiceViewService {

  static String computeInvoiceGridName(Invoice invoice) throws AxelorException {
    if (!InvoiceToolService.isPurchase(invoice)) {
      return "invoice-grid";
    } else {
      return "invoice-supplier-grid";
    }
  }

  static String computeInvoiceFilterName(Invoice invoice) throws AxelorException {
    if (!InvoiceToolService.isPurchase(invoice)) {
      return "customer-invoices-filters";
    } else {
      return "supplier-invoices-filters";
    }
  }
}
