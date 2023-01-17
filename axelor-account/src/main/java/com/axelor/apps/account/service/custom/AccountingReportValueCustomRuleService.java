package com.axelor.apps.account.service.custom;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import java.time.LocalDate;
import java.util.Map;

public interface AccountingReportValueCustomRuleService {
  void createValueFromCustomRuleForColumn(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String parentTitle,
      LocalDate startDate,
      LocalDate endDate,
      int analyticCounter);

  void createValueFromCustomRule(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle,
      int analyticCounter);
}
