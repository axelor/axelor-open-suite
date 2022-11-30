package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
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
  protected MoveLineRepository moveLineRepo;
  protected AccountRepository accountRepo;

  protected int lineOffset = 0;

  @Inject
  public AccountingReportValueServiceImpl(
      AppBaseService appBaseService,
      AccountingReportValueRepository accountingReportValueRepo,
      MoveLineRepository moveLineRepo,
      AccountRepository accountRepo) {
    this.appBaseService = appBaseService;
    this.accountingReportValueRepo = accountingReportValueRepo;
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
    LocalDate startDate = accountingReport.getDateFrom();
    LocalDate endDate = accountingReport.getDateTo();

    this.computeReportValues(accountingReport, startDate, endDate);

    AccountingReportType reportType = accountingReport.getReportType();

    switch (reportType.getComparison()) {
      case AccountingReportTypeRepository.COMPARISON_PREVIOUS_YEAR:
        for (int i = 1; i < accountingReport.getReportType().getNoOfPeriods() + 1; i++) {
          this.computeReportValues(
              accountingReport,
              startDate.minusYears(i).with(TemporalAdjusters.firstDayOfYear()),
              endDate.minusYears(i).with(TemporalAdjusters.lastDayOfYear()));
        }
        break;
      case AccountingReportTypeRepository.COMPARISON_SAME_PERIOD_ON_PREVIOUS_YEAR:
        for (int i = 1; i < accountingReport.getReportType().getNoOfPeriods() + 1; i++) {
          this.computeReportValues(
              accountingReport, startDate.minusYears(i), endDate.minusYears(i));
        }
        break;
      case AccountingReportTypeRepository.COMPARISON_OTHER_PERIOD:
        this.computeReportValues(
            accountingReport,
            accountingReport.getOtherDateFrom(),
            accountingReport.getOtherDateTo());
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void computeReportValues(
      AccountingReport accountingReport, LocalDate startDate, LocalDate endDate)
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
              accountingReport, valuesMapByColumn, valuesMapByLine, startDate, endDate);

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
            accountSet,
            account.getLabel(),
            startDate,
            endDate);
      }
    } else if (CollectionUtils.isNotEmpty(groupColumnList)) {
      columnList.removeAll(groupColumnList);

      for (AccountingReportConfigLine groupColumn : groupColumnList) {
        Set<Account> accountSet = this.getColumnGroupAccounts(groupColumn);

        this.createReportValues(
            accountingReport,
            valuesMapByColumn,
            valuesMapByLine,
            groupColumn,
            columnList,
            lineList,
            accountSet,
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
          null,
          startDate,
          endDate);
    }

    return this.areAllValuesComputed(valuesMapByColumn);
  }

  protected Set<Account> getColumnGroupAccounts(AccountingReportConfigLine groupColumn) {
    return new HashSet<>(
        accountRepo
            .all()
            .filter(this.getAccountQuery(groupColumn))
            .bind("accountSet", groupColumn.getAccountSet())
            .bind("columnAccountFilter", groupColumn.getAccountCode())
            .bind("accountTypeSet", groupColumn.getAccountTypeSet())
            .fetch());
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createReportValues(
      AccountingReport accountingReport,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AccountingReportConfigLine groupColumn,
      List<AccountingReportConfigLine> columnList,
      List<AccountingReportConfigLine> lineList,
      Set<Account> groupAccountSet,
      String parentTitle,
      LocalDate startDate,
      LocalDate endDate)
      throws AxelorException {
    for (AccountingReportConfigLine column : columnList) {
      String columnCode = this.getColumnCode(column.getCode(), parentTitle, groupColumn);

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
              groupAccountSet,
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
      Set<Account> groupAccountSet,
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
          valuesMapByLine.get(line.getCode()),
          valuesMapByColumn,
          valuesMapByLine,
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
          line.getCode());
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      if (line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            AccountExceptionMessage.REPORT_TYPE_INTERSECT_CUSTOM_RULE,
            accountingReport.getReportType().getName(),
            column.getCode(),
            line.getCode());
      } else if (line.getDetailByAccount()
          || line.getDetailByAccountType()
          || line.getDetailByAnalyticAccount()) {
        for (String lineCode :
            valuesMapByLine.keySet().stream()
                .filter(it -> it.matches(String.format("%s_[0-9]+", line.getCode())))
                .collect(Collectors.toList())) {
          this.createValueFromCustomRule(
              accountingReport,
              column,
              line,
              groupColumn,
              valuesMapByLine.get(lineCode),
              valuesMapByColumn,
              valuesMapByLine,
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
            valuesMapByLine.get(line.getCode()),
            valuesMapByColumn,
            valuesMapByLine,
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
          valuesMapByColumn.get(this.getColumnCode(column.getCode(), parentTitle, groupColumn)),
          valuesMapByColumn,
          valuesMapByLine,
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
          groupAccountSet,
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
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle) {
    this.createValueFromCustomRule(
        accountingReport,
        column,
        line,
        groupColumn,
        valuesMap,
        valuesMapByColumn,
        valuesMapByLine,
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
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle,
      String lineCode) {
    BigDecimal result =
        this.getResultFromCustomRule(
            column, line, groupColumn, valuesMap, valuesMapByColumn, valuesMapByLine, parentTitle);

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
        lineCode);
  }

  protected BigDecimal getResultFromCustomRule(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String parentTitle) {
    Map<String, Object> contextMap = new HashMap<>();

    for (String code : valuesMap.keySet()) {
      if (valuesMap.get(code) != null) {
        if (groupColumn != null
            || (!Strings.isNullOrEmpty(parentTitle)
                && column.getRuleTypeSelect()
                    == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE)) {
          String[] tokens = code.split("__");

          if (tokens.length > 1) {
            if (groupColumn != null && tokens[0].equals(column.getCode())) {
              contextMap.put(tokens[1], valuesMap.get(code).getResult());
            } else if (groupColumn == null && tokens[1].equals(parentTitle)) {
              contextMap.put(tokens[0], valuesMap.get(code).getResult());
            }
          }
        } else {
          contextMap.put(code, valuesMap.get(code).getResult());
        }
      }
    }

    String rule;
    if (groupColumn != null
        && groupColumn.getRuleTypeSelect()
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      rule = groupColumn.getRule();
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      rule = column.getRule();
    } else {
      rule = line.getRule();
    }

    Context scriptContext = new Context(contextMap, Object.class);
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

    try {
      return (BigDecimal) scriptHelper.eval(rule);
    } catch (Exception e) {
      this.addNullValue(column, line, groupColumn, valuesMapByColumn, valuesMapByLine, parentTitle);
      return null;
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
      Set<Account> groupAccountSet,
      String parentTitle,
      LocalDate startDate,
      LocalDate endDate)
      throws AxelorException {
    if (column.getResultSelect() == AccountingReportConfigLineRepository.RESULT_SAME_AS_LINE
        && line.getResultSelect() == AccountingReportConfigLineRepository.RESULT_SAME_AS_COLUMN) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_NO_RESULT_SELECT,
          accountingReport.getReportType().getName(),
          column.getCode(),
          line.getCode());
    } else if (column.getResultSelect() != AccountingReportConfigLineRepository.RESULT_SAME_AS_LINE
        && line.getResultSelect() != AccountingReportConfigLineRepository.RESULT_SAME_AS_COLUMN
        && !Objects.equals(column.getResultSelect(), line.getResultSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          AccountExceptionMessage.REPORT_TYPE_DIFFERENT_RESULT_SELECT,
          accountingReport.getReportType().getName(),
          column.getCode(),
          line.getCode());
    }

    Set<Account> accountSet;
    Set<AccountType> accountTypeSet = null;
    Set<AnalyticAccount> analyticAccountSet = null;

    if (groupAccountSet == null) {
      accountSet = this.mergeSets(column.getAccountSet(), line.getAccountSet());
      accountTypeSet = this.mergeSets(column.getAccountTypeSet(), line.getAccountTypeSet());
      analyticAccountSet =
          this.mergeSets(column.getAnalyticAccountSet(), line.getAnalyticAccountSet());
    } else {
      accountSet = this.mergeSets(groupAccountSet, line.getAccountSet());
    }

    if (line.getDetailByAccount()) {
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
            startDate,
            endDate,
            null,
            account.getLabel(),
            lineCode);
        lineOffset++;
      }
    } else if (line.getDetailByAccountType() && accountTypeSet != null) {
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
            startDate,
            endDate,
            null,
            accountType.getName(),
            lineCode);

        lineOffset++;
      }
    } else if (line.getDetailByAnalyticAccount() && analyticAccountSet != null) {
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
            startDate,
            endDate,
            null,
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
          startDate,
          endDate,
          parentTitle,
          line.getLabel(),
          line.getCode());
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
                analyticAccountSet,
                startDate,
                endDate)
            .fetch();

    int resultSelect;
    if (column.getResultSelect() == AccountingReportConfigLineRepository.RESULT_SAME_AS_LINE) {
      resultSelect = line.getResultSelect();
    } else {
      resultSelect = column.getResultSelect();
    }

    BigDecimal result = this.getResultFromMoveLine(moveLineList, resultSelect);

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
        lineCode);
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
    return moveLineRepo
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
        .bind("columnAccountFilter", column.getAccountCode())
        .bind("lineAccountFilter", line.getAccountCode())
        .bind("columnAnalyticAccountFilter", column.getAnalyticAccountCode())
        .bind("lineAnalyticAccountFilter", line.getAnalyticAccountCode())
        .bind("accountTypeSet", accountTypeSet)
        .bind("analyticAccountSet", analyticAccountSet);
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
            accountSet, accountTypeSet, column.getAccountCode(), line.getAccountCode(), true));

    if (CollectionUtils.isNotEmpty(analyticAccountSet)) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount IN :analyticAccountSet AND aml.moveLine = self)");
    }

    if (!Strings.isNullOrEmpty(column.getAnalyticAccountCode())) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount.code SIMILAR TO :columnAnalyticAccountFilter AND aml.moveLine = self)");
    }

    if (!Strings.isNullOrEmpty(line.getAnalyticAccountCode())) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount.code SIMILAR TO :lineAnalyticAccountFilter AND aml.moveLine = self)");
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
            false));
  }

  protected List<String> getAccountFilters(
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      String columnAccountFilter,
      String lineAccountFilter,
      boolean moveLine) {
    List<String> queryList = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(accountSet)) {
      queryList.add(
          String.format(
              "(self%1$s IS NULL OR self%1$s IN :accountSet)", moveLine ? ".account" : ""));
    }

    if (!Strings.isNullOrEmpty(columnAccountFilter)) {
      queryList.add(
          String.format("self%s.code SIMILAR TO :columnAccountFilter", moveLine ? ".account" : ""));
    }

    if (!Strings.isNullOrEmpty(lineAccountFilter)) {
      queryList.add(
          String.format("self%s.code SIMILAR TO :lineAccountFilter", moveLine ? ".account" : ""));
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
      LocalDate startDate,
      LocalDate endDate,
      String parentTitle) {
    Map<String, AccountingReportValue> valuesMap =
        valuesMapByColumn.get(
            this.getColumnCode(column.getPercentageBaseColumn(), parentTitle, groupColumn));

    if (valuesMap == null) {
      this.addNullValue(column, line, groupColumn, valuesMapByColumn, valuesMapByLine, parentTitle);
    } else {
      this.createPercentageValue(
          accountingReport,
          column,
          line,
          groupColumn,
          valuesMap,
          valuesMapByColumn,
          valuesMapByLine,
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
              column, line, groupColumn, valuesMapByColumn, valuesMapByLine, parentTitle);
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
      String lineCode) {
    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String period = String.format("%s - %s", startDate.format(format), endDate.format(format));
    int columnNumber = column.getSequence();
    int lineNumber = line.getSequence();

    AccountingReportValue accountingReportValue =
        new AccountingReportValue(
            columnNumber,
            lineNumber + lineOffset,
            result,
            lineTitle,
            parentTitle,
            period,
            accountingReport,
            line,
            column);

    accountingReportValueRepo.save(accountingReportValue);

    String columnCode = this.getColumnCode(column.getCode(), parentTitle, groupColumn);

    if (valuesMapByColumn.containsKey(columnCode)) {
      valuesMapByColumn.get(columnCode).put(lineCode, accountingReportValue);
    }

    if (valuesMapByLine.containsKey(lineCode)) {
      valuesMapByLine.get(lineCode).put(columnCode, accountingReportValue);
    }
  }

  protected String getColumnCode(
      String columnCode, String parentTitle, AccountingReportConfigLine groupColumn) {
    if (groupColumn != null) {
      return String.format("%s__%s", columnCode, groupColumn.getCode());
    } else if (Strings.isNullOrEmpty(parentTitle)) {
      return columnCode;
    } else {
      return String.format("%s__%s", columnCode, parentTitle);
    }
  }

  protected void addNullValue(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String parentTitle) {
    String columnCode = this.getColumnCode(column.getCode(), parentTitle, groupColumn);

    valuesMapByColumn.get(columnCode).put(line.getCode(), null);
    valuesMapByLine.get(line.getCode()).put(columnCode, null);
  }
}
