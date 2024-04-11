package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import java.math.BigDecimal;

public interface RefundService {
  BigDecimal getRefundPaidAmount(Invoice advancePayment);
}
