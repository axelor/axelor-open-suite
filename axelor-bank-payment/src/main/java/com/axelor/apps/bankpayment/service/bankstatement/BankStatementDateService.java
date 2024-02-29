package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import java.time.LocalDate;

public interface BankStatementDateService {
  void updateBankStatementDate(BankStatement bankStatement, LocalDate operationDate, int lineType);
}
