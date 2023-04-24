package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;

public class MoveLineToolBankPaymentServiceImpl implements MoveLineToolBankPaymentService {
  @Override
  public boolean checkBankReconciledAmount(MoveLine moveLine) {
    return moveLine
            .getBankReconciledAmount()
            .compareTo(moveLine.getDebit().add(moveLine.getCredit()))
        > 0;
  }
}
