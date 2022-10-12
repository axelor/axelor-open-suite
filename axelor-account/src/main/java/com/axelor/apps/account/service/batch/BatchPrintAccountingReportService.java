package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.exception.AxelorException;

public interface BatchPrintAccountingReportService {

  AccountingReport createAccountingReportFromBatch(AccountingBatch accountingBatch)
      throws AxelorException;
}
