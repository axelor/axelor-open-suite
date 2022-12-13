package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportAnalyticConfigLine;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportAnalyticConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportValueServiceImpl implements AccountingReportValueService {
  protected AppBaseService appBaseService;
  protected AccountingReportValueRepository accountingReportValueRepo;
  protected AnalyticAccountRepository analyticAccountRepo;
  protected MoveLineRepository moveLineRepo;
  protected AccountRepository accountRepo;

  protected int lineOffset = 0;
  protected int analyticCounter;

  @Inject
  public AccountingReportValueServiceImpl(
      AppBaseService appBaseService,
      AccountingReportValueRepository accountingReportValueRepo,
      AnalyticAccountRepository analyticAccountRepo,
      MoveLineRepository moveLineRepo,
      AccountRepository accountRepo) {
    this.appBaseService = appBaseService;
    this.accountingReportValueRepo = accountingReportValueRepo;
    this.analyticAccountRepo = analyticAccountRepo;
    this.moveLineRepo = moveLineRepo;
    this.accountRepo = accountRepo;
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
      this.computeReportValues(accountingReport, null);
    } else {
      for (AnalyticAccount configAnalyticAccount :
          this.getSortedAnalyticAccountSet(configAnalyticAccountSet)) {
        this.computeReportValues(accountingReport, configAnalyticAccount);
        analyticCounter++;
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
      AccountingReport accountingReport, AnalyticAccount configAnalyticAccount)
      throws AxelorException {
    LocalDate startDate = accountingReport.getDateFrom();
    LocalDate endDate = accountingReport.getDateTo();

    this.computeReportValues(accountingReport, configAnalyticAccount, startDate, endDate);

    AccountingReportType reportType = accountingReport.getReportType();

    switch (reportType.getComparison()) {
      case AccountingReportTypeRepository.COMPARISON_PREVIOUS_YEAR:
        for (int i = 1; i < accountingReport.getReportType().getNoOfPeriods() + 1; i++) {
          this.computeReportValues(
              accountingReport,
              configAnalyticAccount,
              startDate.minusYears(i).with(TemporalAdjusters.firstDayOfYear()),
              endDate.minusYears(i).with(TemporalAdjusters.lastDayOfYear()));
        }
        break;
      case AccountingReportTypeRepository.COMPARISON_SAME_PERIOD_ON_PREVIOUS_YEAR:
        for (int i = 1; i < accountingReport.getReportType().getNoOfPeriods() + 1; i++) {
          this.computeReportValues(
              accountingReport,
              configAnalyticAccount,
              startDate.minusYears(i),
              endDate.minusYears(i));
        }
        break;
      case AccountingReportTypeRepository.COMPARISON_OTHER_PERIOD:
        this.computeReportValues(
            accountingReport,
            configAnalyticAccount,
            accountingReport.getOtherDateFrom(),
            accountingReport.getOtherDateTo());
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void computeReportValues(
      AccountingReport accountingReport,
      AnalyticAccount configAnalyticAccount,
      LocalDate startDate,
      LocalDate endDate)
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
              endDate);

      if (startTime.until(LocalTime.now(), ChronoUnit.SECONDS)
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
      LocalDate endDate)
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
            endDate);
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
            endDate);
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
          endDate);
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
      LocalDate endDate)
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
              endDate);
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
      LocalDate endDate)
      throws AxelorException {
    if (groupColumn != null
        && groupColumn.getRuleTypeSelect()
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
        && column.getRuleTypeSelect()
            != AccountingReportConfigLineRepository.RULE_TYPE_PERCENTAGE) {
      this.createValueFromCustomRule(
          accountingReport,
          column,
          line,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          startDate,
          endDate,
          parentTitle);
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
          line.getCode());
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
          this.createValueFromCustomRule(
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
              lineCode);
        }
      } else {
        this.createValueFromCustomRule(
            accountingReport,
            column,
            line,
            groupColumn,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            startDate,
            endDate,
            parentTitle);
      }
    } else if (line.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      this.createValueFromCustomRule(
          accountingReport,
          column,
          line,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          startDate,
          endDate,
          parentTitle);
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_PERCENTAGE) {
      this.createPercentageValue(
          accountingReport,
          column,
          line,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          startDate,
          endDate,
          parentTitle);
    } else {
      this.createValueFromMoveLines(
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
          endDate);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromCustomRule(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle) {
    this.createValueFromCustomRule(
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
        line.getCode());
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromCustomRule(
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
      String lineCode) {
    Map<String, AccountingReportValue> valuesMap =
        this.getValuesMap(
            column,
            line,
            groupColumn,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            parentTitle);

    BigDecimal result =
        this.getResultFromCustomRule(
            column,
            line,
            groupColumn,
            valuesMap,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            parentTitle);

    String lineTitle = line.getLabel();
    if (column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      lineTitle =
          valuesMap.values().stream()
              .map(AccountingReportValue::getLineTitle)
              .findAny()
              .orElse(line.getLabel());
    }

    this.createReportValue(
        accountingReport,
        column,
        line,
        groupColumn,
        startDate,
        endDate,
        parentTitle,
        lineTitle,
        result,
        valuesMapByColumn,
        valuesMapByLine,
        configAnalyticAccount,
        lineCode);
  }

  protected Map<String, AccountingReportValue> getValuesMap(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String parentTitle) {
    Map<String, AccountingReportValue> columValues =
        valuesMapByColumn.get(
            this.getColumnCode(column.getCode(), parentTitle, groupColumn, configAnalyticAccount));
    Map<String, AccountingReportValue> lineValues = valuesMapByLine.get(line.getCode());

    if (groupColumn != null
        && groupColumn.getRuleTypeSelect()
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return columValues;
    } else if (column.getRuleTypeSelect()
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
        && line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      if (column.getPriority() > line.getPriority()) {
        return lineValues;
      } else {
        return columValues;
      }
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return lineValues;
    } else {
      return columValues;
    }
  }

  protected BigDecimal getResultFromCustomRule(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String parentTitle) {
    String rule = this.getRule(column, line, groupColumn);
    Map<String, Object> contextMap =
        this.createRuleContextMap(
            column, line, groupColumn, valuesMap, configAnalyticAccount, parentTitle, rule);

    Context scriptContext = new Context(contextMap, Object.class);
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

    try {
      return (BigDecimal) scriptHelper.eval(rule);
    } catch (Exception e) {
      this.addNullValue(
          column,
          line,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          parentTitle);
      return null;
    }
  }

  protected Map<String, Object> createRuleContextMap(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, AccountingReportValue> valuesMap,
      AnalyticAccount configAnalyticAccount,
      String parentTitle,
      String rule) {
    Map<String, Object> contextMap = new HashMap<>();

    for (String code : valuesMap.keySet()) {
      if (valuesMap.get(code) != null) {
        if (groupColumn != null
            || (!Strings.isNullOrEmpty(parentTitle)
                && column.getRuleTypeSelect()
                    == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE)
            || (configAnalyticAccount != null
                && (StringUtils.isEmpty(line.getRule()) || !line.getRule().equals(rule)))) {
          String[] tokens = code.split("__");

          if (tokens.length > 1) {
            if (groupColumn != null && tokens[0].equals(column.getCode())) {
              contextMap.put(tokens[1], valuesMap.get(code).getResult());
            } else if ((groupColumn == null && tokens[1].equals(parentTitle))
                || (configAnalyticAccount != null
                    && Arrays.asList(tokens).contains(configAnalyticAccount.getCode()))) {
              contextMap.put(tokens[0], valuesMap.get(code).getResult());
            }
          }
        } else {
          contextMap.put(code, valuesMap.get(code).getResult());
        }
      }
    }

    if (configAnalyticAccount != null) {
      contextMap.put("analyticAccount", configAnalyticAccount);
    }

    return contextMap;
  }

  protected String getRule(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn) {
    if (groupColumn != null
        && groupColumn.getRuleTypeSelect()
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return groupColumn.getRule();
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return column.getRule();
    } else {
      return line.getRule();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromMoveLines(
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
      LocalDate endDate)
      throws AxelorException {
    this.checkResultSelects(accountingReport, groupColumn, column, line);

    Set<Account> accountSet;
    Set<AccountType> accountTypeSet = null;
    Set<AnalyticAccount> analyticAccountSet =
        this.mergeSets(column.getAnalyticAccountSet(), line.getAnalyticAccountSet());

    if (groupAccount != null) {
      accountSet = new HashSet<>(Collections.singletonList(groupAccount));
    } else {
      accountSet = this.mergeSets(column.getAccountSet(), line.getAccountSet());
      accountTypeSet = this.mergeSets(column.getAccountTypeSet(), line.getAccountTypeSet());
    }

    if (groupColumn != null && groupAccount == null) {
      accountSet = this.mergeSets(groupColumn.getAccountSet(), accountSet);
      accountTypeSet = this.mergeSets(groupColumn.getAccountTypeSet(), accountTypeSet);
      analyticAccountSet = this.mergeSets(groupColumn.getAnalyticAccountSet(), analyticAccountSet);
    }

    if (accountingReport.getDisplayDetails() && line.getDetailByAccount()) {
      int counter = 1;

      for (Account account : accountSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);
        valuesMapByLine.put(lineCode, new HashMap<>());

        this.createValueFromMoveLine(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            new HashSet<>(Collections.singletonList(account)),
            accountTypeSet,
            analyticAccountSet,
            configAnalyticAccount,
            startDate,
            endDate,
            parentTitle,
            account.getLabel(),
            lineCode);
        lineOffset++;
      }
    } else if (accountingReport.getDisplayDetails()
        && line.getDetailByAccountType()
        && accountTypeSet != null) {
      int counter = 1;

      for (AccountType accountType : accountTypeSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);
        valuesMapByLine.put(lineCode, new HashMap<>());

        this.createValueFromMoveLine(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            accountSet,
            new HashSet<>(Collections.singletonList(accountType)),
            analyticAccountSet,
            configAnalyticAccount,
            startDate,
            endDate,
            parentTitle,
            accountType.getName(),
            lineCode);

        lineOffset++;
      }
    } else if (accountingReport.getDisplayDetails()
        && line.getDetailByAnalyticAccount()
        && analyticAccountSet != null) {
      int counter = 1;

      for (AnalyticAccount analyticAccount : analyticAccountSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);
        valuesMapByLine.put(lineCode, new HashMap<>());

        this.createValueFromMoveLine(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            accountSet,
            accountTypeSet,
            new HashSet<>(Collections.singletonList(analyticAccount)),
            configAnalyticAccount,
            startDate,
            endDate,
            parentTitle,
            analyticAccount.getFullName(),
            lineCode);

        lineOffset++;
      }
    } else {
      this.createValueFromMoveLine(
          accountingReport,
          groupColumn,
          column,
          line,
          valuesMapByColumn,
          valuesMapByLine,
          accountSet,
          accountTypeSet,
          analyticAccountSet,
          configAnalyticAccount,
          startDate,
          endDate,
          parentTitle,
          line.getLabel(),
          line.getCode());
    }
  }

  protected void checkResultSelects(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line)
      throws AxelorException {
    List<Integer> basicResultSelectList =
        Arrays.asList(
            AccountingReportConfigLineRepository.RESULT_CREDIT_MINUS_DEBIT,
            AccountingReportConfigLineRepository.RESULT_DEBIT_MINUS_CREDIT);

    boolean isBasicResultSelect =
        basicResultSelectList.contains(column.getResultSelect())
            || basicResultSelectList.contains(line.getResultSelect());
    boolean isOnlyBasicResultSelect =
        basicResultSelectList.contains(column.getResultSelect())
            && basicResultSelectList.contains(line.getResultSelect());
    boolean isGroupResultSelect =
        column.getResultSelect() == AccountingReportConfigLineRepository.RESULT_SAME_AS_GROUP
            || line.getResultSelect() == AccountingReportConfigLineRepository.RESULT_SAME_AS_GROUP;

    if (isOnlyBasicResultSelect
        && !Objects.equals(column.getResultSelect(), line.getResultSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_DIFFERENT_RESULT_SELECT,
          accountingReport.getReportType().getName(),
          column.getCode(),
          line.getCode());
    } else if (!isBasicResultSelect && !isGroupResultSelect) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_NO_RESULT_SELECT,
          accountingReport.getReportType().getName(),
          column.getCode(),
          line.getCode());
    } else if (isGroupResultSelect && groupColumn == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_SAME_AS_GROUP_NO_GROUP,
          accountingReport.getReportType().getName());
    }
  }

  protected <T extends Model> Set<T> mergeSets(Set<T> set1, Set<T> set2) {
    if (CollectionUtils.isEmpty(set1)) {
      return set2;
    } else if (CollectionUtils.isEmpty(set2)) {
      return set1;
    } else {
      Set<T> finalSet = new HashSet<>(set1);
      finalSet.retainAll(set2);
      return finalSet;
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromMoveLine(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      Set<AnalyticAccount> analyticAccountSet,
      AnalyticAccount configAnalyticAccount,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle,
      String lineTitle,
      String lineCode) {
    List<MoveLine> moveLineList =
        this.getMoveLineQuery(
                accountingReport,
                groupColumn,
                column,
                line,
                accountSet,
                accountTypeSet,
                this.mergeSets(
                    analyticAccountSet,
                    configAnalyticAccount == null
                        ? null
                        : new HashSet<>(Collections.singletonList(configAnalyticAccount))),
                startDate,
                endDate)
            .fetch();

    BigDecimal result =
        this.getResultFromMoveLine(moveLineList, this.getResultSelect(column, line, groupColumn));

    this.createReportValue(
        accountingReport,
        column,
        line,
        groupColumn,
        startDate,
        endDate,
        parentTitle,
        lineTitle,
        result,
        valuesMapByColumn,
        valuesMapByLine,
        configAnalyticAccount,
        lineCode);
  }

  protected int getResultSelect(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn) {
    if (column.getResultSelect() == AccountingReportConfigLineRepository.RESULT_SAME_AS_GROUP
        || line.getResultSelect() == AccountingReportConfigLineRepository.RESULT_SAME_AS_GROUP) {
      return groupColumn.getResultSelect();
    } else if (column.getResultSelect()
        == AccountingReportConfigLineRepository.RESULT_SAME_AS_LINE) {
      return line.getResultSelect();
    } else {
      return column.getResultSelect();
    }
  }

  protected Query<MoveLine> getMoveLineQuery(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      Set<AnalyticAccount> analyticAccountSet,
      LocalDate startDate,
      LocalDate endDate) {
    if (column.getComputePreviousYear()) {
      startDate = startDate.minusYears(1);
      endDate = endDate.minusYears(1);
    } else if (column.getComputeOtherPeriod()
        || (groupColumn != null && groupColumn.getComputeOtherPeriod())) {
      startDate = accountingReport.getOtherDateFrom();
      endDate = accountingReport.getOtherDateTo();
    }

    return this.buildMoveLineQuery(
        accountingReport,
        accountSet,
        accountTypeSet,
        analyticAccountSet,
        groupColumn,
        column,
        line,
        startDate,
        endDate);
  }

  protected Query<MoveLine> buildMoveLineQuery(
      AccountingReport accountingReport,
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      Set<AnalyticAccount> analyticAccountSet,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      LocalDate startDate,
      LocalDate endDate) {
    Query<MoveLine> moveLineQuery =
        moveLineRepo
            .all()
            .filter(
                this.getMoveLineQuery(
                    accountingReport,
                    accountSet,
                    accountTypeSet,
                    analyticAccountSet,
                    groupColumn,
                    column,
                    line))
            .bind("dateFrom", startDate)
            .bind("dateTo", endDate)
            .bind("journal", accountingReport.getJournal())
            .bind("paymentMode", accountingReport.getPaymentMode())
            .bind("currency", accountingReport.getCurrency())
            .bind("company", accountingReport.getCompany())
            .bind("statusList", this.getMoveLineStatusList(accountingReport))
            .bind("accountSet", accountSet)
            .bind(
                "groupColumnAnalyticAccountFilter",
                groupColumn == null ? "" : groupColumn.getAnalyticAccountCode())
            .bind("columnAnalyticAccountFilter", column.getAnalyticAccountCode())
            .bind("lineAnalyticAccountFilter", line.getAnalyticAccountCode())
            .bind("accountTypeSet", accountTypeSet)
            .bind("analyticAccountSet", analyticAccountSet);

    if (groupColumn != null) {
      moveLineQuery =
          this.bindAccountFilters(moveLineQuery, groupColumn.getAccountCode(), "groupColumn");
    }

    moveLineQuery = this.bindAccountFilters(moveLineQuery, column.getAccountCode(), "column");

    return this.bindAccountFilters(moveLineQuery, line.getAccountCode(), "line");
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

  protected String getMoveLineQuery(
      AccountingReport accountingReport,
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      Set<AnalyticAccount> analyticAccountSet,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line) {
    List<String> queryList =
        new ArrayList<>(Collections.singletonList("self.move.statusSelect IN :statusList"));

    if (accountingReport.getDateFrom() != null) {
      queryList.add("(self.date IS NULL OR self.date >= :dateFrom)");
    }

    if (accountingReport.getDateTo() != null) {
      queryList.add("(self.date IS NULL OR self.date <= :dateTo)");
    }

    if (accountingReport.getJournal() != null) {
      queryList.add("(self.move.journal IS NULL OR self.move.journal >= :journal)");
    }

    if (accountingReport.getJournal() != null) {
      queryList.add("(self.move.paymentMode IS NULL OR self.move.paymentMode >= :paymentMode)");
    }

    if (accountingReport.getJournal() != null) {
      queryList.add("(self.move.currency IS NULL OR self.move.currency >= :currency)");
    }

    if (accountingReport.getJournal() != null) {
      queryList.add("(self.move.company IS NULL OR self.move.company >= :company)");
    }

    queryList.addAll(
        this.getAccountFilters(
            accountSet,
            accountTypeSet,
            groupColumn == null ? null : groupColumn.getCode(),
            column.getAccountCode(),
            line.getAccountCode(),
            true));

    if (CollectionUtils.isNotEmpty(analyticAccountSet)) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount IN :analyticAccountSet AND aml.moveLine = self)");
    }

    if (groupColumn != null && !Strings.isNullOrEmpty(groupColumn.getAnalyticAccountCode())) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount.code LIKE :groupColumnAnalyticAccountFilter AND aml.moveLine = self)");
    }

    if (!Strings.isNullOrEmpty(column.getAnalyticAccountCode())) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount.code LIKE :columnAnalyticAccountFilter AND aml.moveLine = self)");
    }

    if (!Strings.isNullOrEmpty(line.getAnalyticAccountCode())) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount.code LIKE :lineAnalyticAccountFilter AND aml.moveLine = self)");
    }

    return String.join(" AND ", queryList);
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

  protected BigDecimal getResultFromMoveLine(List<MoveLine> moveLineList, int resultSelect) {
    return moveLineList.stream()
        .map(it -> this.getMoveLineAmount(it, resultSelect))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createPercentageValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle) {
    Map<String, AccountingReportValue> valuesMap =
        valuesMapByColumn.get(
            this.getColumnCode(
                column.getPercentageBaseColumn(), parentTitle, groupColumn, configAnalyticAccount));

    if (valuesMap == null) {
      this.addNullValue(
          column,
          line,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          parentTitle);
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
          parentTitle);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
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
      String parentTitle) {
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
              line,
              groupColumn,
              valuesMapByColumn,
              valuesMapByLine,
              configAnalyticAccount,
              parentTitle);
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
          result);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
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
      BigDecimal result) {
    if (baseValue != null && totalValue != null && totalValue.getResult().signum() != 0) {
      result =
          baseValue
              .getResult()
              .multiply(result)
              .divide(
                  totalValue.getResult(),
                  AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                  RoundingMode.HALF_UP);
    }

    this.createReportValue(
        accountingReport,
        column,
        line,
        groupColumn,
        startDate,
        endDate,
        parentTitle,
        Optional.ofNullable(baseValue)
            .map(AccountingReportValue::getLineTitle)
            .orElse(line.getLabel()),
        result,
        valuesMapByColumn,
        valuesMapByLine,
        configAnalyticAccount,
        lineCode);
  }

  protected List<Integer> getMoveLineStatusList(AccountingReport accountingReport) {
    List<Integer> statusList =
        Arrays.asList(MoveRepository.STATUS_DAYBOOK, MoveRepository.STATUS_ACCOUNTED);

    if (accountingReport.getDisplaySimulatedMove()) {
      statusList.add(MoveRepository.STATUS_SIMULATED);
    }

    return statusList;
  }

  protected BigDecimal getMoveLineAmount(MoveLine moveLine, int resultSelect) {
    BigDecimal value = moveLine.getDebit().subtract(moveLine.getCredit());

    return resultSelect == AccountingReportConfigLineRepository.RESULT_DEBIT_MINUS_CREDIT
        ? value
        : value.negate();
  }

  @Transactional(rollbackOn = {Exception.class})
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
      String lineCode) {
    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String period = String.format("%s - %s", startDate.format(format), endDate.format(format));
    int columnNumber = column.getSequence();
    int lineNumber = line.getSequence();

    AccountingReportValue accountingReportValue =
        new AccountingReportValue(
            columnNumber,
            lineNumber + lineOffset,
            analyticCounter,
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

  protected void addNullValue(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String parentTitle) {
    String columnCode =
        this.getColumnCode(column.getCode(), parentTitle, groupColumn, configAnalyticAccount);

    valuesMapByColumn.get(columnCode).put(line.getCode(), null);
    valuesMapByLine.get(line.getCode()).put(columnCode, null);
  }
}
