/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.custom;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportValuePercentageServiceImpl extends AccountingReportValueAbstractService
    implements AccountingReportValuePercentageService {
  @Inject
  public AccountingReportValuePercentageServiceImpl(
      AccountRepository accountRepo,
      AccountingReportValueRepository accountingReportValueRepo,
      AnalyticAccountRepository analyticAccountRepo,
      DateService dateService) {
    super(accountRepo, accountingReportValueRepo, analyticAccountRepo, dateService);
  }

  @Override
  public void createPercentageValue(
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
      int analyticCounter)
      throws AxelorException {
    Map<String, AccountingReportValue> valuesMap =
        valuesMapByColumn.get(
            this.getColumnCode(
                column.getPercentageBaseColumn(), parentTitle, groupColumn, configAnalyticAccount));

    if (valuesMap == null) {
      if (accountingReport.getTraceAnomalies()
          && StringUtils.notEmpty(column.getPercentageBaseColumn())) {
        this.traceException(
            AccountExceptionMessage.CUSTOM_REPORT_WRONG_PERCENTAGE_BASE_COLUMN,
            accountingReport,
            groupColumn,
            column,
            line);
      }

      this.addNullValue(
          column,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          parentTitle,
          line.getCode());
    } else {
      this.createPercentageValue(
          accountingReport,
          column,
          line,
          groupColumn,
          valuesMap,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          startDate,
          endDate,
          parentTitle,
          analyticCounter);
    }
  }

  protected void createPercentageValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle,
      int analyticCounter)
      throws AxelorException {
    AccountingReportValue totalValue = null;
    List<String> linesCodeList = Collections.singletonList(line.getCode());
    BigDecimal result = BigDecimal.valueOf(100);

    if (StringUtils.notEmpty(line.getPercentageTotalLine())) {
      totalValue = valuesMap.get(line.getPercentageTotalLine());

      if (valuesMap.get(line.getCode()) == null) {
        linesCodeList =
            valuesMap.keySet().stream()
                .filter(it -> Pattern.matches(String.format("%s_[0-9]+", line.getCode()), it))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(linesCodeList)) {
          this.addNullValue(
              column,
              groupColumn,
              valuesMapByColumn,
              valuesMapByLine,
              configAnalyticAccount,
              parentTitle,
              line.getCode());
          return;
        }
      }
    }

    for (String code : linesCodeList) {
      this.createPercentageValue(
          accountingReport,
          column,
          line,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          valuesMap.get(code),
          totalValue,
          startDate,
          endDate,
          parentTitle,
          code,
          result,
          analyticCounter);
    }
  }

  protected void createPercentageValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      AccountingReportValue baseValue,
      AccountingReportValue totalValue,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle,
      String lineCode,
      BigDecimal result,
      int analyticCounter)
      throws AxelorException {
    if (baseValue == null
        || (totalValue == null && StringUtils.notEmpty(line.getPercentageTotalLine()))) {
      this.addNullValue(
          column,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          parentTitle,
          lineCode);

      return;
    } else if (totalValue != null && totalValue.getResult().signum() != 0) {
      result =
          baseValue
              .getResult()
              .multiply(result)
              .divide(
                  totalValue.getResult(),
                  AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                  RoundingMode.HALF_UP);
    } else if (StringUtils.notEmpty(line.getPercentageTotalLine())) {
      result = BigDecimal.ZERO;
    }

    this.createReportValue(
        accountingReport,
        column,
        line,
        groupColumn,
        startDate,
        endDate,
        parentTitle,
        baseValue.getLineTitle(),
        result,
        valuesMapByColumn,
        valuesMapByLine,
        configAnalyticAccount,
        lineCode,
        analyticCounter);
  }
}
