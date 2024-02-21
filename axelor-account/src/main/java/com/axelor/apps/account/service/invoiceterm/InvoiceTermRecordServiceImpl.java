package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.google.inject.Inject;

public class InvoiceTermRecordServiceImpl implements InvoiceTermRecordService {

  protected InvoiceTermService invoiceTermService;

  @Inject
  public InvoiceTermRecordServiceImpl(InvoiceTermService invoiceTermService) {
    this.invoiceTermService = invoiceTermService;
  }

  @Override
  public boolean computeIsCustomized(InvoiceTerm invoiceTerm) {
    return invoiceTerm != null
        && (invoiceTerm.getPaymentConditionLine() == null
            || invoiceTerm.getPercentage() == null
            || invoiceTerm
                    .getPercentage()
                    .compareTo(invoiceTerm.getPaymentConditionLine().getPaymentPercentage())
                != 0);
  }
}
