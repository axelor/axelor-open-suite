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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportValueServiceImpl extends AccountingReportValueAbstractService
    implements AccountingReportValueService {
  protected AccountingReportValueCustomRuleService accountingReportValueCustomRuleService;
  protected AccountingReportValueMoveLineService accountingReportValueMoveLineService;
  protected AccountingReportValuePercentageService accountingReportValuePercentageService;
  protected AppBaseService appBaseService;
  protected AccountingReportValueRepository accountingReportValueRepo;
  protected AnalyticAccountRepository analyticAccountRepo;
  protected AccountRepository accountRepo;

  protected static int lineOffset = 0;

  @Inject
  public AccountingReportValueServiceImpl(
      AccountingReportValueRepository accountingReportValueRepo,
      AccountingReportValueCustomRuleService accountingReportValueCustomRuleService,
      AccountingReportValueMoveLineService accountingReportValueMoveLineService,
      AccountingReportValuePercentageService accountingReportValuePercentageService,
      AppBaseService appBaseService,
      AnalyticAccountRepository analyticAccountRepo,
      AccountRepository accountRepo) {
    super(accountingReportValueRepo);
    this.accountingReportValueCustomRuleService = accountingReportValueCustomRuleService;
    this.accountingReportValueMoveLineService = accountingReportValueMoveLineService;
    this.accountingReportValuePercentageService = accountingReportValuePercentageService;
    this.appBaseService = appBaseService;
    this.accountingReportValueRepo = accountingReportValueRepo;
    this.analyticAccountRepo = analyticAccountRepo;
    this.accountRepo = accountRepo;
  }

  public static synchronized void incrementLineOffset() {
    lineOffset++;
  }

  public static synchronized int getLineOffset() {
    return lineOffset;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void clearReportValues(AccountingReport accountingReport) {
    accountingReportValueRepo.findByAccountingReport(accountingReport).remove();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
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

  protected Set<AnalyticAccount> fetchAnalyticAccountsFromCode(String code) {
    return new HashSet<>(
        analyticAccountRepo.all().filter("self.code LIKE :code").bind("code", code).fetch());
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

  @Transactional(rollbackOn = {Exception.class})
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

  @Transactional(rollbackOn = {Exception.class})
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

    boolean isAllComputed = false;
    LocalTime startTime = LocalTime.now();

    while (!isAllComputed) {
      isAllComputed =
          this.createReportValues(
              accountingReport,
              valuesMapByColumn,
              valuesMapByLine,
              configAnalyticAccount,
              startDate,
              endDate,
              analyticCounter);

      if (!isAllComputed
          && startTime.until(LocalTime.now(), ChronoUnit.SECONDS)
              > appBaseService.getProcessTimeout()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            AccountExceptionMessage.CUSTOM_REPORT_TIMEOUT,
            accountingReport.getRef());
      }
    }
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

  protected boolean areAllValuesComputed(
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn) {
    return valuesMapByColumn.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .noneMatch(Objects::isNull);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected boolean createReportValues(
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

    return this.areAllValuesComputed(valuesMapByColumn);
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

  @Transactional(rollbackOn = {Exception.class})
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
      int analyticCounter)
      throws AxelorException {
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
        }
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
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
      int analyticCounter)
      throws AxelorException {
    if (groupColumn != null
        && groupColumn.getRuleTypeSelect()
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
        && column.getRuleTypeSelect()
            != AccountingReportConfigLineRepository.RULE_TYPE_PERCENTAGE) {
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
    } else if ((column.getNotComputedIfIntersect() && line.getNotComputedIfIntersect())
        || column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE
        || line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE) {
      this.createReportValue(
          accountingReport,
          column,
          line,
          groupColumn,
          startDate,
          endDate,
          parentTitle,
          line.getLabel(),
          null,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          line.getCode(),
          analyticCounter);
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      if (accountingReport.getDisplayDetails()
          && (line.getDetailByAccount()
              || line.getDetailByAccountType()
              || line.getDetailByAnalyticAccount())) {
        for (String lineCode :
            valuesMapByLine.keySet().stream()
                .filter(it -> it.matches(String.format("%s_[0-9]+", line.getCode())))
                .collect(Collectors.toList())) {
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
              lineCode,
              analyticCounter);
        }
      } else {
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
      }
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
