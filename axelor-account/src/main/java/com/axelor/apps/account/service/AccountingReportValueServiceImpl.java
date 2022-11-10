package com.axelor.apps.account.service;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

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
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    Map<String, Map<String, AccountingReportValue>> valuesMapByColumn = new HashMap<>();
    Map<String, Map<String, AccountingReportValue>> valuesMapByLine = new HashMap<>();

    AccountingReportType accountingReportType = accountingReport.getReportType();
    this.checkAccountingReportType(accountingReportType);

    boolean isAllComputed = false;
    int timeoutCounter = 0;

    while (!isAllComputed) {
      isAllComputed = this.createReportValues(accountingReport, valuesMapByColumn, valuesMapByLine);

      if (timeoutCounter++ > appBaseService.getProcessTimeout()) {
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
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine)
      throws AxelorException {
    List<AccountingReportConfigLine> columnList =
        accountingReport.getReportType().getAccountingReportConfigLineColumnList().stream()
            .sorted(Comparator.comparing(AccountingReportConfigLine::getSequence))
            .collect(Collectors.toList());
    List<AccountingReportConfigLine> lineList =
        accountingReport.getReportType().getAccountingReportConfigLineList().stream()
            .sorted(Comparator.comparing(AccountingReportConfigLine::getSequence))
            .collect(Collectors.toList());

    AccountingReportConfigLine groupColumn =
        columnList.stream()
            .filter(it -> it.getTypeSelect() == AccountingReportConfigLineRepository.TYPE_GROUP)
            .findAny()
            .orElse(null);

    if (groupColumn != null) {
      columnList.remove(groupColumn);
      List<Account> accountList = this.getColumnGroupAccounts(groupColumn);

      for (Account account : accountList) {
        this.createReportValues(
            accountingReport, valuesMapByColumn, valuesMapByLine, columnList, lineList, account);
      }
    } else {
      this.createReportValues(
          accountingReport, valuesMapByColumn, valuesMapByLine, columnList, lineList, null);
    }

    return this.areAllValuesComputed(valuesMapByColumn);
  }

  protected List<Account> getColumnGroupAccounts(AccountingReportConfigLine groupColumn) {
    return accountRepo
        .all()
        .filter(this.getAccountQuery(groupColumn))
        .bind("accountSet", groupColumn.getAccountSet())
        .bind("columnAccountFilter", groupColumn.getAccountCode())
        .bind("accountTypeSet", groupColumn.getAccountTypeSet())
        .fetch();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createReportValues(
      AccountingReport accountingReport,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      List<AccountingReportConfigLine> columnList,
      List<AccountingReportConfigLine> lineList,
      Account groupAccount)
      throws AxelorException {
    for (AccountingReportConfigLine column : columnList) {
      String columnCode = this.getColumnCode(column.getCode(), groupAccount.getLabel());

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
              accountingReport, column, line, valuesMapByColumn, valuesMapByLine, groupAccount);
        }
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      Account groupAccount)
      throws AxelorException {
    String parentTitle = groupAccount == null ? null : groupAccount.getLabel();

    if ((column.getNotComputedIfIntersect() && line.getNotComputedIfIntersect())
        || column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE
        || line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE) {
      this.createReportValue(
          accountingReport,
          column,
          line,
          line.getLabel(),
          parentTitle,
          null,
          null,
          null,
          valuesMapByColumn,
          valuesMapByLine,
          line.getCode());
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      if (line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
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
              valuesMapByLine.get(lineCode),
              valuesMapByColumn,
              valuesMapByLine,
              parentTitle,
              lineCode);
        }
      } else {
        this.createValueFromCustomRule(
            accountingReport,
            column,
            line,
            valuesMapByLine.get(line.getCode()),
            valuesMapByColumn,
            valuesMapByLine,
            parentTitle);
      }
    } else if (line.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      this.createValueFromCustomRule(
          accountingReport,
          column,
          line,
          valuesMapByColumn.get(this.getColumnCode(column.getCode(), parentTitle)),
          valuesMapByColumn,
          valuesMapByLine,
          parentTitle);
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_PERCENTAGE) {
      this.createPercentageValue(
          accountingReport, column, line, valuesMapByColumn, valuesMapByLine, parentTitle);
    } else {
      this.createValueFromMoveLines(
          accountingReport, column, line, valuesMapByColumn, valuesMapByLine, groupAccount);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromCustomRule(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String parentTitle) {
    this.createValueFromCustomRule(
        accountingReport,
        column,
        line,
        valuesMap,
        valuesMapByColumn,
        valuesMapByLine,
        parentTitle,
        line.getCode());
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromCustomRule(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String parentTitle,
      String lineCode) {
    BigDecimal result =
        this.getResultFromCustomRule(
            column, line, valuesMap, valuesMapByColumn, valuesMapByLine, parentTitle, 0);
    BigDecimal resultn1 = null;
    BigDecimal resultn2 = null;

    if (accountingReport.getReportType().getComparison()
        != AccountingReportTypeRepository.COMPARISON_NO_COMPARISON) {
      resultn1 =
          this.getResultFromCustomRule(
              column, line, valuesMap, valuesMapByColumn, valuesMapByLine, parentTitle, 1);

      if (accountingReport.getReportType().getNoOfPeriods() == 2) {
        resultn2 =
            this.getResultFromCustomRule(
                column, line, valuesMap, valuesMapByColumn, valuesMapByLine, parentTitle, 2);
      }
    }

    this.createReportValue(
        accountingReport,
        column,
        line,
        parentTitle,
        valuesMap.values().stream()
            .map(AccountingReportValue::getLineTitle)
            .findAny()
            .orElse(line.getLabel()),
        result,
        resultn1,
        resultn2,
        valuesMapByColumn,
        valuesMapByLine,
        lineCode);
  }

  protected BigDecimal getResultFromCustomRule(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String parentTitle,
      int periodOffset) {
    AccountingReportConfigLine configLine =
        column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
            ? column
            : line;
    Map<String, Object> contextMap = new HashMap<>();

    for (String code : valuesMap.keySet()) {
      if (valuesMap.get(code) != null) {
        if (!Strings.isNullOrEmpty(parentTitle)) {
          String[] tokens = code.split("__");

          if (tokens.length > 1 && tokens[1].equals(parentTitle)) {
            contextMap.put(
                tokens[0], this.getResultFromPeriodOffset(code, valuesMap, periodOffset));
          }
        } else {
          contextMap.put(code, this.getResultFromPeriodOffset(code, valuesMap, periodOffset));
        }
      }
    }

    Context scriptContext = new Context(contextMap, Object.class);
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

    try {
      return (BigDecimal) scriptHelper.eval(configLine.getRule());
    } catch (Exception e) {
      this.addNullValue(column, line, valuesMapByColumn, valuesMapByLine, parentTitle);
      return null;
    }
  }

  protected BigDecimal getResultFromPeriodOffset(
      String code, Map<String, AccountingReportValue> valuesMap, int periodOffset) {
    AccountingReportValue accountingReportValue = valuesMap.get(code);

    if (periodOffset == 0) {
      return accountingReportValue.getResult();
    } else if (periodOffset == 1) {
      return accountingReportValue.getResultn1();
    } else {
      return accountingReportValue.getResultn2();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromMoveLines(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      Account groupAccount)
      throws AxelorException {
    if (!Objects.equals(column.getResultSelect(), line.getResultSelect())) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
    }

    Set<Account> accountSet;
    Set<AccountType> accountTypeSet = null;
    Set<AnalyticAccount> analyticAccountSet = null;
    String parentTitle = null;

    if (groupAccount == null) {
      accountSet = this.mergeSets(column.getAccountSet(), line.getAccountSet());
      accountTypeSet = this.mergeSets(column.getAccountTypeSet(), line.getAccountTypeSet());
      analyticAccountSet =
          this.mergeSets(column.getAnalyticAccountSet(), line.getAnalyticAccountSet());
    } else {
      accountSet = this.mergeSets(Sets.newHashSet(groupAccount), line.getAccountSet());
      parentTitle = groupAccount.getLabel();
    }

    if (line.getDetailByAccount()) {
      int counter = 1;

      for (Account account : accountSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);
        valuesMapByLine.put(lineCode, new HashMap<>());

        this.createValueFromMoveLine(
            accountingReport,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            new HashSet<>(Collections.singletonList(account)),
            accountTypeSet,
            analyticAccountSet,
            null,
            account.getLabel(),
            lineCode);
        lineOffset++;
      }
    } else if (line.getDetailByAccountType()) {
      int counter = 1;

      for (AccountType accountType : accountTypeSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);
        valuesMapByLine.put(lineCode, new HashMap<>());

        this.createValueFromMoveLine(
            accountingReport,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            accountSet,
            new HashSet<>(Collections.singletonList(accountType)),
            analyticAccountSet,
            null,
            accountType.getName(),
            lineCode);

        lineOffset++;
      }
    } else if (line.getDetailByAnalyticAccount()) {
      int counter = 1;

      for (AnalyticAccount analyticAccount : analyticAccountSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);
        valuesMapByLine.put(lineCode, new HashMap<>());

        this.createValueFromMoveLine(
            accountingReport,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            accountSet,
            accountTypeSet,
            new HashSet<>(Collections.singletonList(analyticAccount)),
            null,
            analyticAccount.getFullName(),
            lineCode);

        lineOffset++;
      }
    } else {
      this.createValueFromMoveLine(
          accountingReport,
          column,
          line,
          valuesMapByColumn,
          valuesMapByLine,
          accountSet,
          accountTypeSet,
          analyticAccountSet,
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
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      Set<AnalyticAccount> analyticAccountSet,
      String parentTitle,
      String lineTitle,
      String lineCode) {
    List<MoveLine> moveLineList =
        this.getMoveLineQuery(
                accountingReport, column, line, accountSet, accountTypeSet, analyticAccountSet)
            .fetch();

    int periodOffset = column.getComputePreviousYear() ? 1 : 0;

    BigDecimal result =
        this.getResultFromMoveLine(
            accountingReport, moveLineList, periodOffset, column.getResultSelect(), false);
    BigDecimal resultn1 = null;
    BigDecimal resultn2 = null;

    if (accountingReport.getReportType().getComparison()
        != AccountingReportTypeRepository.COMPARISON_NO_COMPARISON) {
      resultn1 =
          this.getResultFromMoveLine(
              accountingReport,
              moveLineList,
              periodOffset + 1,
              column.getResultSelect(),
              accountingReport.getReportType().getComparison()
                  == AccountingReportTypeRepository.COMPARISON_OTHER_PERIOD);

      if (accountingReport.getReportType().getNoOfPeriods() == 2) {
        resultn2 =
            this.getResultFromMoveLine(
                accountingReport, moveLineList, periodOffset + 2, column.getResultSelect(), false);
      }
    }

    this.createReportValue(
        accountingReport,
        column,
        line,
        parentTitle,
        lineTitle,
        result,
        resultn1,
        resultn2,
        valuesMapByColumn,
        valuesMapByLine,
        lineCode);
  }

  protected Query<MoveLine> getMoveLineQuery(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      Set<AnalyticAccount> analyticAccountSet) {
    AccountingReportType reportType = accountingReport.getReportType();

    int offset = column.getComputePreviousYear() ? 1 : 0;
    LocalDate dateFrom = accountingReport.getDateFrom().minusYears(offset);
    LocalDate dateTo = accountingReport.getDateTo().minusYears(offset);

    if (reportType.getComparison() == AccountingReportTypeRepository.COMPARISON_PREVIOUS_YEAR
        || reportType.getComparison()
            == AccountingReportTypeRepository.COMPARISON_SAME_PERIOD_ON_PREVIOUS_YEAR) {
      dateFrom = dateFrom.minusYears(reportType.getNoOfPeriods());
    } else if (reportType.getComparison()
        == AccountingReportTypeRepository.COMPARISON_OTHER_PERIOD) {
      dateFrom = accountingReport.getOtherDateFrom().minusYears(offset);
    }

    return moveLineRepo
        .all()
        .filter(
            this.buildQuery(
                accountingReport,
                accountSet,
                accountTypeSet,
                analyticAccountSet,
                column.getAccountCode(),
                line.getAccountCode(),
                column.getAnalyticAccountCode(),
                line.getAnalyticAccountCode()))
        .bind("dateFrom", dateFrom)
        .bind("dateTo", dateTo)
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

  protected String buildQuery(
      AccountingReport accountingReport,
      Set<Account> accountSet,
      Set<AccountType> accountTypeSet,
      Set<AnalyticAccount> analyticAccountSet,
      String columnAccountFilter,
      String lineAccountFilter,
      String columnAnalyticAccountFilter,
      String lineAnalyticAccountFilter) {
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
            accountSet, accountTypeSet, columnAccountFilter, lineAccountFilter, true));

    if (CollectionUtils.isNotEmpty(analyticAccountSet)) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount IN :analyticAccountSet AND aml.moveLine = self)");
    }

    if (!Strings.isNullOrEmpty(columnAnalyticAccountFilter)) {
      queryList.add(
          "EXISTS(SELECT 1 FROM AnalyticMoveLine aml WHERE aml.analyticAccount.code LIKE :columnAnalyticAccountFilter AND aml.moveLine = self)");
    }

    if (!Strings.isNullOrEmpty(lineAnalyticAccountFilter)) {
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
          String.format("self%s.code LIKE :columnAccountFilter", moveLine ? ".account" : ""));
    }

    if (!Strings.isNullOrEmpty(lineAccountFilter)) {
      queryList.add(
          String.format("self%s.code LIKE :lineAccountFilter", moveLine ? ".account" : ""));
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

  protected BigDecimal getResultFromMoveLine(
      AccountingReport accountingReport,
      List<MoveLine> moveLineList,
      int periodOffset,
      int resultSelect,
      boolean otherPeriod) {
    LocalDate dateFrom = accountingReport.getDateFrom();
    LocalDate dateTo = accountingReport.getDateTo();

    if (accountingReport.getReportType().getComparison()
        == AccountingReportTypeRepository.COMPARISON_PREVIOUS_YEAR) {
      dateFrom = dateFrom.with(firstDayOfYear());
      dateTo = dateTo.with(lastDayOfYear());
    } else if (otherPeriod) {
      dateFrom = accountingReport.getOtherDateFrom();
      dateTo = accountingReport.getOtherDateTo();
    }

    if (otherPeriod) {
      periodOffset -= 1;
    }

    if (periodOffset > 0) {
      dateFrom = dateFrom.minusYears(periodOffset);
      dateTo = dateTo.minusYears(periodOffset);
    }

    final LocalDate finalDateFrom = dateFrom;
    final LocalDate finalDateTo = dateTo;

    return moveLineList.stream()
        .filter(it -> !it.getDate().isBefore(finalDateFrom) && !it.getDate().isAfter(finalDateTo))
        .map(it -> this.getMoveLineAmount(it, resultSelect))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createPercentageValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String parentTitle) {
    Map<String, AccountingReportValue> valuesMap =
        valuesMapByColumn.get(this.getColumnCode(column.getPercentageBaseColumn(), parentTitle));

    if (valuesMap == null) {
      this.addNullValue(column, line, valuesMapByColumn, valuesMapByLine, parentTitle);
    } else {
      this.createPercentageValue(
          accountingReport,
          column,
          line,
          valuesMap,
          valuesMapByColumn,
          valuesMapByLine,
          parentTitle);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createPercentageValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String parentTitle) {
    AccountingReportValue totalValue = null;
    List<String> linesCodeList = Collections.singletonList(line.getCode());
    BigDecimal result = BigDecimal.valueOf(100);
    BigDecimal resultn1 = BigDecimal.valueOf(100);
    BigDecimal resultn2 = BigDecimal.valueOf(100);

    if (StringUtils.notEmpty(line.getPercentageTotalLine())) {
      totalValue = valuesMap.get(line.getPercentageTotalLine());

      if (valuesMap.get(line.getCode()) == null) {
        linesCodeList =
            valuesMap.keySet().stream()
                .filter(it -> Pattern.matches(String.format("%s_[0-9]+", line.getCode()), it))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(linesCodeList)) {
          this.addNullValue(column, line, valuesMapByColumn, valuesMapByLine, parentTitle);
          return;
        }
      }
    }

    for (String code : linesCodeList) {
      this.createPercentageValue(
          accountingReport,
          column,
          line,
          valuesMapByColumn,
          valuesMapByLine,
          valuesMap.get(code),
          totalValue,
          parentTitle,
          code,
          result,
          resultn1,
          resultn2);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createPercentageValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AccountingReportValue baseValue,
      AccountingReportValue totalValue,
      String parentTitle,
      String lineCode,
      BigDecimal result,
      BigDecimal resultn1,
      BigDecimal resultn2) {
    if (baseValue != null && totalValue != null && totalValue.getResult().signum() != 0) {
      result =
          baseValue
              .getResult()
              .multiply(result)
              .divide(
                  totalValue.getResult(),
                  AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                  RoundingMode.HALF_UP);

      if (totalValue.getResultn1().signum() != 0) {
        resultn1 =
            baseValue
                .getResultn1()
                .multiply(resultn1)
                .divide(
                    totalValue.getResultn1(),
                    AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                    RoundingMode.HALF_UP);
      }

      if (totalValue.getResultn2().signum() != 0) {
        resultn2 =
            baseValue
                .getResultn2()
                .multiply(resultn2)
                .divide(
                    totalValue.getResultn2(),
                    AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                    RoundingMode.HALF_UP);
      }
    }

    this.createReportValue(
        accountingReport,
        column,
        line,
        parentTitle,
        Optional.ofNullable(baseValue)
            .map(AccountingReportValue::getLineTitle)
            .orElse(line.getLabel()),
        result,
        resultn1,
        resultn2,
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
      String parentTitle,
      String lineTitle,
      BigDecimal result,
      BigDecimal resultn1,
      BigDecimal resultn2,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String lineCode) {
    int columnNumber =
        accountingReport.getReportType().getAccountingReportConfigLineColumnList().indexOf(column);
    int lineNumber =
        accountingReport.getReportType().getAccountingReportConfigLineList().indexOf(line);

    AccountingReportValue accountingReportValue =
        new AccountingReportValue(
            columnNumber,
            lineNumber + lineOffset,
            result,
            resultn1,
            resultn2,
            lineTitle,
            parentTitle,
            accountingReport,
            line,
            column);

    accountingReportValueRepo.save(accountingReportValue);

    String columnCode = this.getColumnCode(column.getCode(), parentTitle);

    valuesMapByColumn.get(columnCode).put(lineCode, accountingReportValue);
    valuesMapByLine.get(lineCode).put(columnCode, accountingReportValue);
  }

  protected String getColumnCode(String columnCode, String parentTitle) {
    if (Strings.isNullOrEmpty(parentTitle)) {
      return columnCode;
    } else {
      return String.format("%s__%s", columnCode, parentTitle);
    }
  }

  protected void addNullValue(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      String parentTitle) {
    String columnCode = this.getColumnCode(column.getCode(), parentTitle);

    valuesMapByColumn.get(columnCode).put(line.getCode(), null);
    valuesMapByLine.get(line.getCode()).put(columnCode, null);
  }
}
