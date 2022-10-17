package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.exception.AxelorException;

public interface AccountingReportValueService {
  void computeReportValues(AccountingReport accountingReport) throws AxelorException;
}
