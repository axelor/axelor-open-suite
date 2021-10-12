package com.axelor.apps.cash.management.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import java.time.LocalDate;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceEstimatedPaymentServiceImpl implements InvoiceEstimatedPaymentService {

  @Override
  public Invoice computeEstimatedPaymentDate(Invoice invoice) {
    if (CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return invoice;
    }
    if (invoice.getPartner() != null && invoice.getPartner().getPaymentDelay() != null) {

      int paymentDelay = invoice.getPartner().getPaymentDelay().intValue();

      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (invoiceTerm.getEstimatedPaymentDate() == null) {
          invoiceTerm.setEstimatedPaymentDate(invoiceTerm.getDueDate().plusDays(paymentDelay));
        }
      }
    } else {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (invoiceTerm.getEstimatedPaymentDate() != null) {
          continue;
        }

        LocalDate estimatedPaymentDate = invoiceTerm.getDueDate();

        invoiceTerm.setEstimatedPaymentDate(estimatedPaymentDate);
      }
    }
    return invoice;
  }
}
