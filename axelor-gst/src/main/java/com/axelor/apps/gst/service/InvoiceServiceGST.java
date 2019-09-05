package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface InvoiceServiceGST {

  Invoice calculate(Invoice invoice) throws AxelorException;

  Invoice setInvoiceDetails(Invoice invoice, List<Long> productIds, Integer partnerId)
      throws AxelorException;

  Long saveInvoice(Invoice invoice) throws AxelorException;
}
