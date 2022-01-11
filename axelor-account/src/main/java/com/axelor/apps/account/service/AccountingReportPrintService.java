package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.exception.AxelorException;

public interface AccountingReportPrintService {
  String print(AccountingReport accountingReport) throws AxelorException;

  String computeName(AccountingReport accountingReport);
}
