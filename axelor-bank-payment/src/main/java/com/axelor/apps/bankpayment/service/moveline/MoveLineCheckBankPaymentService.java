package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineCheckBankPaymentService {
  void checkBankReconciledAmount(MoveLine moveLine) throws AxelorException;
}
