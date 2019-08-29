package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;

public interface InvoiceServiceGST {

  Invoice calculate(Invoice invoice) throws AxelorException;
}
