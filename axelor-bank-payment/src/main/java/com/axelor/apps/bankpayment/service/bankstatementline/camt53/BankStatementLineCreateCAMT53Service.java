package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.base.AxelorException;

public interface BankStatementLineCreateCAMT53Service {
  void processCAMT53(BankStatement bankStatement) throws AxelorException;
}
