package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;

public interface MoveLineToolBankPaymentService {
  boolean checkBankReconciledAmount(MoveLine moveLine);
}
