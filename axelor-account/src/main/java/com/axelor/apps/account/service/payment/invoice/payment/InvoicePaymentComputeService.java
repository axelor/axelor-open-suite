package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface InvoicePaymentComputeService {
  List<Long> computeDataForFinancialDiscount(InvoicePayment invoicePayment, Long invoiceId)
      throws AxelorException;
}
