package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;

public interface MoveLineRecordBankPaymentService {
  void revertDebitCreditAmountChange(MoveLine moveLine);

  void revertBankReconciledAmountChange(MoveLine moveLine);
}
