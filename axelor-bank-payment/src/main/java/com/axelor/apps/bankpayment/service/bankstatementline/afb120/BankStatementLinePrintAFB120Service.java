package com.axelor.apps.bankpayment.service.bankstatementline.afb120;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import java.time.LocalDate;

public interface BankStatementLinePrintAFB120Service {
  String print(LocalDate fromDate, LocalDate toDate, BankDetails bankDetails, String exportType)
      throws AxelorException;
}
