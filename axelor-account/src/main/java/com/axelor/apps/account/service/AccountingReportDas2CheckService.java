package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface AccountingReportDas2CheckService {

  List<String> checkMandatoryDataForDas2Export(AccountingReport accountingExport)
      throws AxelorException;
}
