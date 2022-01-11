package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;

public interface AccountingReportToolService {
  boolean isThereAlreadyDraftReportInPeriod(AccountingReport accountingReport);
}
