package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import com.axelor.apps.base.service.discount.GlobalDiscountServiceImpl;
import com.google.inject.Inject;

public class InvoiceGlobalDiscountServiceImpl extends GlobalDiscountServiceImpl {

  protected final InvoiceService invoiceService;
  protected final InvoiceLineService invoiceLineService;

  @Inject
  public InvoiceGlobalDiscountServiceImpl(
      InvoiceService invoiceService, InvoiceLineService invoiceLineService) {
    this.invoiceService = invoiceService;
    this.invoiceLineService = invoiceLineService;
  }

  @Override
  protected void compute(GlobalDiscounter globalDiscounter) throws AxelorException {
    if (globalDiscounter instanceof Invoice) {
      Invoice invoice = (Invoice) globalDiscounter;
      invoice
          .getInvoiceLineList()
          .forEach(
              invoiceLine -> {
                try {
                  invoiceLineService.compute(invoice, invoiceLine);
                } catch (AxelorException e) {
                  throw new RuntimeException(e);
                }
              });
      invoiceService.compute(invoice);
    }
  }
}
