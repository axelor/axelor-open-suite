package com.axelor.apps.cash.management.service;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceEstimatedPaymentService {
  Invoice computeEstimatedPaymentDate(Invoice invoice);
}
