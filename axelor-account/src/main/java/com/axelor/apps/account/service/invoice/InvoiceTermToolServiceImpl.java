package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;

public class InvoiceTermToolServiceImpl implements InvoiceTermToolService {
  @Override
  public boolean isPartiallyPaid(InvoiceTerm invoiceTerm) {
    return invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) != 0;
  }
}
