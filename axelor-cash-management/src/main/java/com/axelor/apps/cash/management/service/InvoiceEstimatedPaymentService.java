package com.axelor.apps.cash.management.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;

import java.time.LocalDate;

public interface InvoiceEstimatedPaymentService {
  Invoice computeEstimatedPaymentDate(Invoice invoice);
  LocalDate computeEstimatedPaymentDate(InvoiceTerm invoiceTerm,Invoice invoice);
}
