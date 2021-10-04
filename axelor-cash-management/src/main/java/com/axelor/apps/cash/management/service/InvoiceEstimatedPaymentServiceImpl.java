package com.axelor.apps.cash.management.service;

import com.axelor.apps.account.db.Invoice;
import java.time.LocalDate;

public class InvoiceEstimatedPaymentServiceImpl implements InvoiceEstimatedPaymentService {

  @Override
  public LocalDate computeEstimatedPaymentDate(Invoice invoice) {
    LocalDate estimatedPaymentDate = invoice.getDueDate();
    if (estimatedPaymentDate != null
        && invoice.getPartner() != null
        && invoice.getPartner().getPaymentDelay() != null) {
      estimatedPaymentDate =
          estimatedPaymentDate.plusDays(invoice.getPartner().getPaymentDelay().intValue());
    }
    return estimatedPaymentDate;
  }
}
