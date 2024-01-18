package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.bankpayment.db.BankStatement;

public interface BankStatementLineDeleteService {
  void deleteBankStatementLines(BankStatement bankStatement);
}
