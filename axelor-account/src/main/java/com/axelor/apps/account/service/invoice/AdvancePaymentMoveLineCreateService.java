package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface AdvancePaymentMoveLineCreateService {
  void manageAdvancePaymentInvoiceTaxMoveLines(
      Move move, MoveLine defaultMoveLine, BigDecimal prorata, LocalDate paymentDate)
      throws AxelorException;
}
