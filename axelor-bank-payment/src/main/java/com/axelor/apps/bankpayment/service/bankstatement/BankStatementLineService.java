package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import java.time.LocalDate;

public interface BankStatementLineService {

  String print(LocalDate fromDate, LocalDate toDate, BankDetails bankDetails, String exportType)
      throws AxelorException;

  void removeBankReconciliationLines(BankStatement bankStatement);
}
