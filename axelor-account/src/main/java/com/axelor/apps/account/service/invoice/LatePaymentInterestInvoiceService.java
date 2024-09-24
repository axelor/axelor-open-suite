package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;

public interface LatePaymentInterestInvoiceService {
  Invoice generateLatePaymentInterestInvoice(Invoice invoice) throws AxelorException;
}
