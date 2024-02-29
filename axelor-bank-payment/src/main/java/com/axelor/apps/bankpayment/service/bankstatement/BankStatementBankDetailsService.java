package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;

public interface BankStatementBankDetailsService {
  void updateBankDetailsBalance(BankStatement bankStatement);
}
