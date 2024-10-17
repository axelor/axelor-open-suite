package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;

public class InvoiceTermPaymentToolServiceImpl implements InvoiceTermPaymentToolService {

  @Override
  public boolean isPartialPayment(InvoicePayment invoicePayment) {
    return invoicePayment.getInvoiceTermPaymentList().stream()
        .allMatch(
            it ->
                it.getPaidAmount()
                        .add(it.getFinancialDiscountAmount())
                        .compareTo(it.getInvoiceTerm().getAmount())
                    != 0);
  }
}
