package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;

public interface MoveLineCheckBankPaymentService {
  void checkBankReconciledAmount(MoveLine moveLine) throws AxelorException;
}
