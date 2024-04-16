package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import java.math.BigDecimal;

public interface AdvancePaymentRefundService {
  BigDecimal getRefundPaidAmount(Invoice advancePayment);
}
