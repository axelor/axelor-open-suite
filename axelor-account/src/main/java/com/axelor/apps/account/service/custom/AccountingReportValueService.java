package com.axelor.apps.account.service.custom;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.exception.AxelorException;

public interface AccountingReportValueService {
  void clearReportValues(AccountingReport accountingReport);

  void computeReportValues(AccountingReport accountingReport) throws AxelorException;
}
