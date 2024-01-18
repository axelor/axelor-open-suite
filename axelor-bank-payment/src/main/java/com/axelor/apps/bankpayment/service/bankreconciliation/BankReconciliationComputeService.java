package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;

public interface BankReconciliationComputeService {

  void compute(BankReconciliation bankReconciliation);

  BankReconciliation computeInitialBalance(BankReconciliation bankReconciliation);

  BankReconciliation computeEndingBalance(BankReconciliation bankReconciliation);
}
