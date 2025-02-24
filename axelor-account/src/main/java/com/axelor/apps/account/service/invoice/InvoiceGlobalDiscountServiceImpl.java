package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import com.axelor.apps.base.interfaces.GlobalDiscounterLine;
import com.axelor.apps.base.service.discount.GlobalDiscountAbstractService;
import com.google.inject.Inject;
import java.util.List;

public class InvoiceGlobalDiscountServiceImpl extends GlobalDiscountAbstractService
    implements InvoiceGlobalDiscountService {

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
    Invoice invoice = getInvoice(globalDiscounter);
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

  @Override
  protected List<? extends GlobalDiscounterLine> getGlobalDiscounterLines(
      GlobalDiscounter globalDiscounter) {
    return getInvoice(globalDiscounter).getInvoiceLineList();
  }

  protected Invoice getInvoice(GlobalDiscounter globalDiscounter) {
    Invoice invoice = null;
    if (globalDiscounter instanceof Invoice) {
      invoice = (Invoice) globalDiscounter;
    }
    return invoice;
  }
}
