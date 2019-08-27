package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceService {
  Invoice calculate(Invoice invoice);
}
