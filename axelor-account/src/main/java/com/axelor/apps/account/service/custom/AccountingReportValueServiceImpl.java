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
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportAnalyticConfigLine;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportAnalyticConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountingReportValueServiceImpl extends AccountingReportValueAbstractService
    implements AccountingReportValueService {
  protected AccountingReportValueCustomRuleService accountingReportValueCustomRuleService;
  protected AccountingReportValueMoveLineService accountingReportValueMoveLineService;
  protected AccountingReportValuePercentageService accountingReportValuePercentageService;
  protected AppBaseService appBaseService;
  protected TraceBackRepository traceBackRepository;

  protected static int lineOffset = 0;
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public AccountingReportValueServiceImpl(
      AccountRepository accountRepo,
      AccountingReportValueRepository accountingReportValueRepo,
      AccountingReportValueCustomRuleService accountingReportValueCustomRuleService,
      AccountingReportValueMoveLineService accountingReportValueMoveLineService,
      AccountingReportValuePercentageService accountingReportValuePercentageService,
      AppBaseService appBaseService,
      AnalyticAccountRepository analyticAccountRepo,
      DateService dateService,
      TraceBackRepository traceBackRepository) {
    super(accountRepo, accountingReportValueRepo, analyticAccountRepo, dateService);
    this.accountingReportValueCustomRuleService = accountingReportValueCustomRuleService;
    this.accountingReportValueMoveLineService = accountingReportValueMoveLineService;
    this.accountingReportValuePercentageService = accountingReportValuePercentageService;
    this.appBaseService = appBaseService;
    this.traceBackRepository = traceBackRepository;
  }

  public static synchronized void incrementLineOffset() {
    lineOffset++;
  }

  public static synchronized int getLineOffset() {
    return lineOffset;
  }

  @Override
  @Transactional
  public void clearReportValues(AccountingReport accountingReport) {
    accountingReportValueRepo.findByAccountingReport(accountingReport).remove();
  }

  @Override
  public void computeReportValues(AccountingReport accountingReport) throws AxelorException {
    Set<AnalyticAccount> configAnalyticAccountSet =
        this.getConfigAnalyticAccountSet(
            accountingReport.getAccountingReportAnalyticConfigLineList());

    if (CollectionUtils.isEmpty(configAnalyticAccountSet)) {
      this.computeReportValues(accountingReport, null, 0);
    } else {
      int analyticCounter = 0;

      for (AnalyticAccount configAnalyticAccount :
          this.getSortedAnalyticAccountSet(configAnalyticAccountSet)) {
        this.computeReportValues(accountingReport, configAnalyticAccount, analyticCounter++);
      }
    }
  }

  protected Set<AnalyticAccount> getConfigAnalyticAccountSet(
      List<AccountingReportAnalyticConfigLine> analyticConfigLineList) {
    Set<AnalyticAccount> configAnalyticAccountSet = new HashSet<>();

    if (CollectionUtils.isEmpty(analyticConfigLineList)) {
      return null;
    }

    analyticConfigLineList.forEach(
        it -> configAnalyticAccountSet.addAll(this.fetchConfigAnalyticAccountSet(it)));

    return configAnalyticAccountSet;
  }

  protected Set<AnalyticAccount> fetchConfigAnalyticAccountSet(
      AccountingReportAnalyticConfigLine analyticConfigLine) {
    switch (analyticConfigLine.getTypeSelect()) {
      case AccountingReportAnalyticConfigLineRepository.TYPE_CODE:
        return this.fetchAnalyticAccountsFromCode(analyticConfigLine.getAnalyticAccountCode());
      case AccountingReportAnalyticConfigLineRepository.TYPE_RANGE:
        return analyticConfigLine.getAnalyticAccountSet();
      case AccountingReportAnalyticConfigLineRepository.TYPE_ACCOUNT:
        return new HashSet<>(Collections.singletonList(analyticConfigLine.getAnalyticAccount()));
      default:
        return null;
    }
  }

  protected Set<AnalyticAccount> getSortedAnalyticAccountSet(
      Set<AnalyticAccount> analyticAccountSet) {
    Set<AnalyticAccount> sortedConfigAnalyticAccountSet = new HashSet<>();

    this.sortAnalyticAccountSetRecursive(analyticAccountSet, sortedConfigAnalyticAccountSet, null);

    return sortedConfigAnalyticAccountSet;
  }

  protected void sortAnalyticAccountSetRecursive(
      Set<AnalyticAccount> analyticAccountSet,
      Set<AnalyticAccount> sortedAnalyticAccountSet,
      AnalyticAccount parentAnalyticAccount) {
    Set<AnalyticAccount> currentLevelAnalyticAccountSet =
        analyticAccountSet.stream()
            .filter(
                it ->
                    it.getParent() == parentAnalyticAccount
                        || (parentAnalyticAccount == null
                            && !analyticAccountSet.contains(it.getParent())))
            .collect(Collectors.toSet());

    if (CollectionUtils.isEmpty(currentLevelAnalyticAccountSet)) {
      return;
    }

    for (AnalyticAccount currentLevelAnalyticAccount : currentLevelAnalyticAccountSet) {
      sortedAnalyticAccountSet.add(currentLevelAnalyticAccount);
      this.sortAnalyticAccountSetRecursive(
          analyticAccountSet, sortedAnalyticAccountSet, currentLevelAnalyticAccount);
    }
  }

  protected void computeReportValues(
      AccountingReport accountingReport, AnalyticAccount configAnalyticAccount, int analyticCounter)
      throws AxelorException {
    LocalDate startDate = accountingReport.getDateFrom();
    LocalDate endDate = accountingReport.getDateTo();

    this.computeReportValues(
        accountingReport, configAnalyticAccount, startDate, endDate, analyticCounter);

    AccountingReportType reportType = accountingReport.getReportType();

    switch (reportType.getComparison()) {
      case AccountingReportTypeRepository.COMPARISON_PREVIOUS_YEAR:
        for (int i = 1; i < accountingReport.getReportType().getNoOfPeriods() + 1; i++) {
          this.computeReportValues(
              accountingReport,
              configAnalyticAccount,
              startDate.minusYears(i).with(TemporalAdjusters.firstDayOfYear()),
              endDate.minusYears(i).with(TemporalAdjusters.lastDayOfYear()),
              analyticCounter);
        }
        break;
      case AccountingReportTypeRepository.COMPARISON_SAME_PERIOD_ON_PREVIOUS_YEAR:
        for (int i = 1; i < accountingReport.getReportType().getNoOfPeriods() + 1; i++) {
          this.computeReportValues(
              accountingReport,
              configAnalyticAccount,
              startDate.minusYears(i),
              endDate.minusYears(i),
              analyticCounter);
        }
        break;
      case AccountingReportTypeRepository.COMPARISON_OTHER_PERIOD:
        this.computeReportValues(
            accountingReport,
            configAnalyticAccount,
            accountingReport.getOtherDateFrom(),
            accountingReport.getOtherDateTo(),
            analyticCounter);
    }
  }

  protected void computeReportValues(
      AccountingReport accountingReport,
      AnalyticAccount configAnalyticAccount,
      LocalDate startDate,
      LocalDate endDate,
      int analyticCounter)
      throws AxelorException {
    Map<String, Map<String, AccountingReportValue>> valuesMapByColumn = new HashMap<>();
    Map<String, Map<String, AccountingReportValue>> valuesMapByLine = new HashMap<>();

    AccountingReportType accountingReportType = accountingReport.getReportType();
    this.checkAccountingReportType(accountingReportType);

    this.clearTracebacks(accountingReport);
    accountingReport.setTraceAnomalies(false);

    int nullCount = -1;
    int previousNullCount;

    while (nullCount != 0) {
      previousNullCount = nullCount;

      nullCount =
          this.createReportValues(
              accountingReport,
              valuesMapByColumn,
              valuesMapByLine,
              configAnalyticAccount,
              startDate,
              endDate,
              analyticCounter);

      if (nullCount == previousNullCount) {
        accountingReport.setTraceAnomalies(true);

        this.createReportValues(
            accountingReport,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            startDate,
            endDate,
            analyticCounter);

        throw new AxelorException(
            accountingReport,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.CUSTOM_REPORT_TIMEOUT),
            accountingReport.getRef());
      }
    }
  }

  @Transactional
  protected void clearTracebacks(AccountingReport accountingReport) {
    traceBackRepository
        .all()
        .filter("self.ref = 'com.axelor.apps.account.db.AccountingReport' AND self.refId = :id")
        .bind("id", accountingReport.getId())
        .remove();
  }

  protected void checkAccountingReportType(AccountingReportType accountingReportType)
      throws AxelorException {
    if (accountingReportType.getTypeSelect() != AccountingReportRepository.REPORT_CUSTOM_STATE) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_NOT_CUSTOM,
          accountingReportType.getName());
    }

    if (CollectionUtils.isEmpty(accountingReportType.getAccountingReportConfigLineColumnList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_NO_COLUMN,
          accountingReportType.getName());
    }

    if (CollectionUtils.isEmpty(accountingReportType.getAccountingReportConfigLineList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_NO_LINE,
          accountingReportType.getName());
    }
  }

  protected int getNullCount(Map<String, Map<String, AccountingReportValue>> valuesMapByColumn) {
    return (int)
        valuesMapByColumn.values().stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .filter(Objects::isNull)
            .count();
  }

  protected int createReportValues(
      AccountingReport accountingReport,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      LocalDate startDate,
      LocalDate endDate,
      int analyticCounter)
      throws AxelorException {
    List<AccountingReportConfigLine> columnList =
        accountingReport.getReportType().getAccountingReportConfigLineColumnList().stream()
            .sorted(Comparator.comparing(AccountingReportConfigLine::getSequence))
            .collect(Collectors.toList());
    List<AccountingReportConfigLine> lineList =
        accountingReport.getReportType().getAccountingReportConfigLineList().stream()
            .sorted(Comparator.comparing(AccountingReportConfigLine::getSequence))
            .collect(Collectors.toList());

    List<AccountingReportConfigLine> groupColumnList =
        columnList.stream()
            .filter(it -> it.getTypeSelect() == AccountingReportConfigLineRepository.TYPE_GROUP)
            .collect(Collectors.toList());
    AccountingReportConfigLine groupByAccountColumn =
        columnList.stream()
            .filter(
                it ->
                    it.getTypeSelect()
                        == AccountingReportConfigLineRepository.TYPE_GROUP_BY_ACCOUNT)
            .findAny()
            .orElse(null);

    if (CollectionUtils.isNotEmpty(groupColumnList) && groupByAccountColumn != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_MULTIPLE_GROUPS);
    }

    if (groupByAccountColumn != null) {
      columnList.remove(groupByAccountColumn);
      Set<Account> accountSet = this.getColumnGroupAccounts(groupByAccountColumn);

      for (Account account : accountSet) {
        this.createReportValues(
            accountingReport,
            valuesMapByColumn,
            valuesMapByLine,
            null,
            columnList,
            lineList,
            account,
            configAnalyticAccount,
            account.getLabel(),
            startDate,
            endDate,
            analyticCounter);
      }
    } else if (CollectionUtils.isNotEmpty(groupColumnList)) {
      columnList.removeAll(groupColumnList);

      for (AccountingReportConfigLine groupColumn : groupColumnList) {
        this.createReportValues(
            accountingReport,
            valuesMapByColumn,
            valuesMapByLine,
            groupColumn,
            columnList,
            lineList,
            null,
            configAnalyticAccount,
            groupColumn.getLabel(),
            startDate,
            endDate,
            analyticCounter);
      }
    } else {
      this.createReportValues(
          accountingReport,
          valuesMapByColumn,
          valuesMapByLine,
          null,
          columnList,
          lineList,
          null,
          configAnalyticAccount,
          null,
          startDate,
          endDate,
          analyticCounter);
    }

    return this.getNullCount(valuesMapByColumn);
  }

  protected Set<Account> getColumnGroupAccounts(AccountingReportConfigLine groupColumn) {
    Query<Account> accountQuery =
        accountRepo
            .all()
            .filter(this.getAccountQuery(groupColumn))
            .bind("accountSet", groupColumn.getAccountSet())
            .bind("accountTypeSet", groupColumn.getAccountTypeSet());

    this.bindAccountFilters(accountQuery, groupColumn.getAccountCode(), "groupColumn");

    return new HashSet<>(accountQuery.fetch());
  }

  protected void createReportValues(
      AccountingReport accountingReport,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AccountingReportConfigLine groupColumn,
      List<AccountingReportConfigLine> columnList,
      List<AccountingReportConfigLine> lineList,
      Account groupAccount,
      AnalyticAccount configAnalyticAccount,
      String parentTitle,
      LocalDate startDate,
      LocalDate endDate,
      int analyticCounter) {
    for (AccountingReportConfigLine column : columnList) {
      if (StringUtils.notEmpty(column.getGroupsWithoutColumn()) && groupColumn != null) {
        List<String> groupsWithoutColumnCodeList =
            Arrays.asList(column.getGroupsWithoutColumn().split(","));

        if (groupsWithoutColumnCodeList.contains(groupColumn.getCode())) {
          continue;
        }
      }

      String columnCode =
          this.getColumnCode(column.getCode(), parentTitle, groupColumn, configAnalyticAccount);

      if (!valuesMapByColumn.containsKey(columnCode)) {
        valuesMapByColumn.put(columnCode, new HashMap<>());
      }

      for (AccountingReportConfigLine line : lineList) {
        accountingReport = this.fetchAccountingReport(accountingReport);

        line = JPA.find(AccountingReportConfigLine.class, line.getId());
        column = JPA.find(AccountingReportConfigLine.class, column.getId());
        groupColumn =
            groupColumn != null
                ? JPA.find(AccountingReportConfigLine.class, groupColumn.getId())
                : null;
        groupAccount = groupAccount != null ? JPA.find(Account.class, groupAccount.getId()) : null;
        configAnalyticAccount =
            configAnalyticAccount != null
                ? JPA.find(AnalyticAccount.class, configAnalyticAccount.getId())
                : null;
        if (!valuesMapByLine.containsKey(line.getCode())) {
          valuesMapByLine.put(line.getCode(), new HashMap<>());
        }

        if (!valuesMapByColumn.get(columnCode).containsKey(line.getCode())
            || valuesMapByColumn.get(columnCode).get(line.getCode()) == null) {
          this.createValue(
              accountingReport,
              groupColumn,
              column,
              line,
              valuesMapByColumn,
              valuesMapByLine,
              groupAccount,
              configAnalyticAccount,
              parentTitle,
              startDate,
              endDate,
              analyticCounter);
          JPA.clear();
        }
      }
    }
  }

  protected void createValue(
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
      int analyticCounter) {
    if (this.isValueAlreadyComputed(
        groupColumn, column, line, valuesMapByColumn, configAnalyticAccount, parentTitle)) {
      return;
    }

    try {
      log.debug(
          String.format(
              "Computing group: %s, column: %s, line %s",
              groupColumn != null ? groupColumn.getCode() : "", column.getCode(), line.getCode()));
      if (groupColumn != null
          && groupColumn.getRuleTypeSelect()
              == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
          && column.getRuleTypeSelect()
              != AccountingReportConfigLineRepository.RULE_TYPE_PERCENTAGE) {
        accountingReportValueCustomRuleService.createValueFromCustomRuleForColumn(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            parentTitle,
            startDate,
            endDate,
            analyticCounter);
      } else if (this.isNotCompute(column, line)) {
        this.createReportValue(
            accountingReport,
            column,
            line,
            groupColumn,
            startDate,
            endDate,
            parentTitle,
            line.getLabel(),
            BigDecimal.ZERO,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            line.getCode(),
            analyticCounter);
      } else if (column.getRuleTypeSelect()
          == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
        accountingReportValueCustomRuleService.createValueFromCustomRuleForColumn(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            parentTitle,
            startDate,
            endDate,
            analyticCounter);
      } else if (column.getRuleTypeSelect()
          == AccountingReportConfigLineRepository.RULE_TYPE_PERCENTAGE) {
        accountingReportValuePercentageService.createPercentageValue(
            accountingReport,
            column,
            line,
            groupColumn,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            startDate,
            endDate,
            parentTitle,
            analyticCounter);
      } else if (line.getRuleTypeSelect()
          == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
        accountingReportValueCustomRuleService.createValueFromCustomRule(
            accountingReport,
            column,
            line,
            groupColumn,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            startDate,
            endDate,
            parentTitle,
            analyticCounter);
      } else {
        accountingReportValueMoveLineService.createValueFromMoveLines(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            groupAccount,
            configAnalyticAccount,
            parentTitle,
            startDate,
            endDate,
            analyticCounter);
      }
    } catch (Exception e) {
      this.traceException(e, accountingReport, groupColumn, column, line);
    }
  }

  protected boolean isNotCompute(
      AccountingReportConfigLine column, AccountingReportConfigLine line) {
    return (column.getNotComputedIfIntersect() && line.getNotComputedIfIntersect())
        || column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE
        || line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE;
  }

  protected boolean isValueAlreadyComputed(
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      AnalyticAccount configAnalyticAccount,
      String parentTitle) {
    String columnCode =
        this.getColumnCode(column.getCode(), parentTitle, groupColumn, configAnalyticAccount);
    if (valuesMapByColumn.get(columnCode).get(line.getCode()) != null) {
      return true;
    } else {
      List<String> linesCodeList =
          valuesMapByColumn.get(columnCode).keySet().stream()
              .filter(it -> Pattern.matches(String.format("%s_[0-9]+", line.getCode()), it))
              .collect(Collectors.toList());

      return CollectionUtils.isNotEmpty(linesCodeList)
          && linesCodeList.stream()
              .allMatch(it -> valuesMapByColumn.get(columnCode).get(it) != null);
    }
  }

  protected String getAccountQuery(AccountingReportConfigLine configLine) {
    return String.join(
        " AND ",
        this.getAccountFilters(
            configLine.getAccountSet(),
            configLine.getAccountTypeSet(),
            configLine.getAccountCode(),
            null,
            null,
            false));
  }
}
