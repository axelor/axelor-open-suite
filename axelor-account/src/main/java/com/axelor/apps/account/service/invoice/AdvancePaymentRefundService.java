package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;

public interface AdvancePaymentRefundService {
  void updateAdvancePaymentAmounts(Invoice refund) throws AxelorException;
}
