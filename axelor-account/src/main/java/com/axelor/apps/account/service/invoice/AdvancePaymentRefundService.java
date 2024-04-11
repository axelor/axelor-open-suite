package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import java.util.Set;

public interface AdvancePaymentRefundService {
  void updateAdvancePaymentAmounts(Invoice refund) throws AxelorException;

  String createAdvancePaymentInvoiceSetDomain(Invoice refund) throws AxelorException;

  Set<Invoice> getDefaultAdvancePaymentInvoice(Invoice refund) throws AxelorException;
}
