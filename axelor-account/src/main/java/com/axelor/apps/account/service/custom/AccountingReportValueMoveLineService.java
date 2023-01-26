package com.axelor.apps.account.service.custom;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;
import java.util.Map;

public interface AccountingReportValueMoveLineService {
  void createValueFromMoveLines(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      Account groupAccount,
      AnalyticAccount configAnalyticAccount,
      String parentTitle,
      LocalDate startDate,
      LocalDate endDate,
      int analyticCounter)
      throws AxelorException;
}
