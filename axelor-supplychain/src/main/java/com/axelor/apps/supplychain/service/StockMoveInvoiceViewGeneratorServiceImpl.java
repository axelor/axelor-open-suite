package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.exception.AxelorException;

public class StockMoveInvoiceViewGeneratorServiceImpl
    implements StockMoveInvoiceViewGeneratorService {

  @Override
  public String invoiceGridGenerator(Invoice invoice) throws AxelorException {
    if (!InvoiceToolService.isPurchase(invoice)) {
      return "invoice-grid";
    } else {
      return "invoice-supplier-grid";
    }
  }

  @Override
  public String invoiceFilterGenerator(Invoice invoice) throws AxelorException {
    if (!InvoiceToolService.isPurchase(invoice)) {
      return "customer-invoices-filters";
    } else {
      return "supplier-invoices-filters";
    }
  }
}
