package com.axelor.apps.bankpayment.service.bankstatement.file;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.base.AxelorException;
import java.io.IOException;

public interface BankStatementFileService {

  void process(BankStatement bankStatement) throws IOException, AxelorException;
}
