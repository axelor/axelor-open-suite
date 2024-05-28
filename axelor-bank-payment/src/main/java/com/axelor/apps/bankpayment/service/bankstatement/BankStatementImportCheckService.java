package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.base.AxelorException;

public interface BankStatementImportCheckService {
  void checkImport(BankStatement bankStatement) throws AxelorException;
}
