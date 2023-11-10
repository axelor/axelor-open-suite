package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;

public interface BankStatementValidateService {
  BankStatement setIsFullyReconciled(BankStatement bankStatement);
}
