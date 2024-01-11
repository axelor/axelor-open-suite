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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public abstract class AccountingReportValueAbstractService {
  protected AccountRepository accountRepo;
  protected AccountingReportValueRepository accountingReportValueRepo;
  protected AnalyticAccountRepository analyticAccountRepo;
  protected DateService dateService;

  @Inject
  public AccountingReportValueAbstractService(
      AccountRepository accountRepo,
      AccountingReportValueRepository accountingReportValueRepo,
      AnalyticAccountRepository analyticAccountRepo,
      DateService dateService) {
    this.accountRepo = accountRepo;
    this.accountingReportValueRepo = accountingReportValueRepo;
    this.analyticAccountRepo = analyticAccountRepo;
    this.dateService = dateService;
  }

  protected void addNullValue(
      AccountingReportConfigLine column,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String parentTitle,
      String lineCode) {
    String columnCode =
        this.getColumnCode(column.getCode(), parentTitle, groupColumn, configAnalyticAccount);

    valuesMapByColumn.get(columnCode).put(lineCode, null);
    valuesMapByLine.get(lineCode).put(columnCode, null);
  }

  @Transactional
  protected void createReportValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle,
      String lineTitle,
      BigDecimal result,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String lineCode,
      int analyticCounter)
      throws AxelorException {
    DateTimeFormatter format = dateService.getDateFormat();
    String period = String.format("%s - %s", startDate.format(format), endDate.format(format));
    int groupNumber = groupColumn == null ? 0 : groupColumn.getSequence();
    int columnNumber = column.getSequence();
    int lineNumber = line.getSequence();

    AccountingReportValue accountingReportValue =
        new AccountingReportValue(
            groupNumber,
            columnNumber,
            lineNumber + AccountingReportValueServiceImpl.getLineOffset(),
            analyticCounter,
            this.getStyleSelect(groupColumn, column, line),
            groupColumn == null
                ? AccountingReportConfigLineRepository.STYLE_NO_STYLE
                : groupColumn.getStyleSelect(),
            this.getColumnStyleSelect(groupColumn, column),
            line.getStyleSelect(),
            result,
            lineTitle,
            parentTitle,
            period,
            accountingReport,
            line,
            column,
            configAnalyticAccount);

    accountingReportValueRepo.save(accountingReportValue);

    String columnCode =
        this.getColumnCode(column.getCode(), parentTitle, groupColumn, configAnalyticAccount);

    if (valuesMapByColumn.containsKey(columnCode)) {
      valuesMapByColumn.get(columnCode).put(lineCode, accountingReportValue);
    }

    if (valuesMapByLine.containsKey(lineCode)) {
      valuesMapByLine.get(lineCode).put(columnCode, accountingReportValue);
    }
  }

  protected String getColumnCode(
      String columnCode,
      String parentTitle,
      AccountingReportConfigLine groupColumn,
      AnalyticAccount configAnalyticAccount) {
    List<String> columnCodeTokens = new ArrayList<>(Collections.singletonList(columnCode));

    if (groupColumn != null) {
      columnCodeTokens.add(groupColumn.getCode());
    } else if (!Strings.isNullOrEmpty(parentTitle)) {
      columnCodeTokens.add(parentTitle);
    }

    if (configAnalyticAccount != null) {
      columnCodeTokens.add(configAnalyticAccount.getCode());
    }

    return String.join("__", columnCodeTokens);
  }

  protected int getStyleSelect(
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line) {
    if (groupColumn != null
        && this.isThisConfigLineStylePriorityHighest(groupColumn, column, line)) {
      return groupColumn.getStyleSelect();
    } else if (this.isThisConfigLineStylePriorityHighest(column, groupColumn, line)) {
      return column.getStyleSelect();
    } else {
      return line.getStyleSelect();
    }
  }

  protected boolean isThisConfigLineStylePriorityHighest(
      AccountingReportConfigLine mainConfigLine,
      AccountingReportConfigLine secondaryConfigLine,
      AccountingReportConfigLine tertiaryConfigLine) {
    return mainConfigLine.getStyleSelect() != AccountingReportConfigLineRepository.STYLE_NO_STYLE
        && (secondaryConfigLine == null
            || mainConfigLine.getStylePriority() >= secondaryConfigLine.getStylePriority())
        && mainConfigLine.getStyleSelect() >= tertiaryConfigLine.getStylePriority();
  }

  protected int getColumnStyleSelect(
      AccountingReportConfigLine groupColumn, AccountingReportConfigLine column) {
    if (groupColumn != null
        && groupColumn.getStyleSelect() == AccountingReportConfigLineRepository.STYLE_NO_STYLE
        && groupColumn.getStylePriority() >= column.getStylePriority()) {
      return groupColumn.getStyleSelect();
    } else {
      return column.getStyleSelect();
    }
  }

  protected List<String> getAccountFilters(
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      String groupColumnFilter,
      String columnAccountFilter,
      String lineAccountFilter,
      boolean moveLine) {
    List<String> queryList = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(accountSet)) {
      queryList.add(
          String.format(
              "(self%1$s IS NULL OR self%1$s IN :accountSet)", moveLine ? ".account" : ""));
    }

    if (!Strings.isNullOrEmpty(groupColumnFilter)) {
      queryList.add(this.getAccountFilterQueryList(groupColumnFilter, "groupColumn", moveLine));
    }

    if (!Strings.isNullOrEmpty(columnAccountFilter)) {
      queryList.add(this.getAccountFilterQueryList(columnAccountFilter, "column", moveLine));
    }

    if (!Strings.isNullOrEmpty(lineAccountFilter)) {
      queryList.add(this.getAccountFilterQueryList(lineAccountFilter, "line", moveLine));
    }

    if (CollectionUtils.isNotEmpty(accountTypeSet)) {
      queryList.add(
          String.format(
              "(self%1$s IS NULL OR self%1$s.accountType IN :accountTypeSet)",
              moveLine ? ".account" : ""));
    }

    if (!moveLine) {
      queryList.add("self.isRegulatoryAccount IS FALSE");
    }

    return queryList;
  }

  protected String getAccountFilterQueryList(String accountFilter, String type, boolean moveLine) {
    String[] tokens = accountFilter.split(",");
    List<String> filterQueryList = new ArrayList<>();

    for (int i = 0; i < tokens.length; i++) {
      filterQueryList.add(
          String.format(
              "self%s.code LIKE :%sAccountFilter%d", moveLine ? ".account" : "", type, i));
    }

    return String.format("(%s)", String.join(" OR ", filterQueryList));
  }

  protected <T extends Model> Query<T> bindAccountFilters(
      Query<T> moveLineQuery, String accountFilter, String type) {
    if (StringUtils.isEmpty(accountFilter)) {
      return moveLineQuery;
    }

    String[] tokens = accountFilter.split(",");

    for (int i = 0; i < tokens.length; i++) {
      moveLineQuery = moveLineQuery.bind(String.format("%sAccountFilter%d", type, i), tokens[i]);
    }

    return moveLineQuery;
  }

  protected Set<AnalyticAccount> fetchAnalyticAccountsFromCode(String code) {
    return new HashSet<>(
        analyticAccountRepo.all().filter("self.code LIKE :code").bind("code", code).fetch());
  }

  protected AccountingReport fetchAccountingReport(AccountingReport accountingReport) {
    boolean traceAnomalies = accountingReport.getTraceAnomalies();

    accountingReport = JPA.find(AccountingReport.class, accountingReport.getId());
    accountingReport.setTraceAnomalies(traceAnomalies);

    return accountingReport;
  }

  protected void traceException(
      String errorMessage,
      AccountingReport accountingReport,
      AccountingReportConfigLine group,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line) {
    this.traceException(
        new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(errorMessage)),
        accountingReport,
        group,
        column,
        line);
  }

  protected void traceException(
      Exception e,
      AccountingReport accountingReport,
      AccountingReportConfigLine group,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line) {
    AxelorException axelorException;
    String message =
        Optional.of(e)
            .map(Throwable::getCause)
            .map(Throwable::getLocalizedMessage)
            .orElse(e.getLocalizedMessage());

    if (group == null) {
      axelorException =
          new AxelorException(
              accountingReport,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(AccountExceptionMessage.CUSTOM_REPORT_ANOMALY_NO_GROUP),
              column.getCode(),
              line.getCode(),
              message);
    } else {
      axelorException =
          new AxelorException(
              accountingReport,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(AccountExceptionMessage.CUSTOM_REPORT_ANOMALY_GROUP),
              group.getCode(),
              column.getCode(),
              line.getCode(),
              message);
    }

    TraceBackService.trace(axelorException);
  }
}
