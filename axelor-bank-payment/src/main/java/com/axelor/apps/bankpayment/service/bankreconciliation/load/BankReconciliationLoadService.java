package com.axelor.apps.bankpayment.service.bankreconciliation.load;

import com.axelor.apps.bankpayment.db.BankReconciliation;

public interface BankReconciliationLoadService {

  void loadBankStatement(BankReconciliation bankReconciliation, boolean includeBankStatement);
}
