package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.AxelorException;

public interface BankReconciliationBalanceComputationService {

  BankReconciliation computeBalances(BankReconciliation bankReconciliation) throws AxelorException;
}
