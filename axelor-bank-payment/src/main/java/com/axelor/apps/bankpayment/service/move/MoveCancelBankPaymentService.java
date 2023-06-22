package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;

public interface MoveCancelBankPaymentService {
  void cancelGeneratedMove(Move move) throws AxelorException;
}
