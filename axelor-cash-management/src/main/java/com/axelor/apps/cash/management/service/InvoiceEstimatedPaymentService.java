package com.axelor.apps.cash.management.service;

import com.axelor.apps.account.db.Invoice;
import java.time.LocalDate;

public interface InvoiceEstimatedPaymentService {
  LocalDate computeEstimatedPaymentDate(Invoice invoice);
}
