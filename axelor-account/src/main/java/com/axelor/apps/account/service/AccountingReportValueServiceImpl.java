package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportValueServiceImpl implements AccountingReportValueService {
  protected AppBaseService appBaseService;
  protected AccountingReportValueRepository accountingReportValueRepo;
  protected MoveLineRepository moveLineRepo;

  protected int lineOffset = 0;

  @Inject
  public AccountingReportValueServiceImpl(
      AppBaseService appBaseService,
      AccountingReportValueRepository accountingReportValueRepo,
      MoveLineRepository moveLineRepo) {
    this.appBaseService = appBaseService;
    this.accountingReportValueRepo = accountingReportValueRepo;
    this.moveLineRepo = moveLineRepo;
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
      isAllComputed =
          this.createReportValues(
              accountingReport, accountingReportType, valuesMapByColumn, valuesMapByLine);

      if (timeoutCounter++ > appBaseService.getProcessTimeout()) {
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
      }
    }
  }

  protected void checkAccountingReportType(AccountingReportType accountingReportType)
      throws AxelorException {
    if (accountingReportType.getTypeSelect() != AccountingReportRepository.REPORT_CUSTOM_STATE) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
    }

    if (CollectionUtils.isEmpty(accountingReportType.getAccountingReportConfigLineColumnList())) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
    }

    if (CollectionUtils.isEmpty(accountingReportType.getAccountingReportConfigLineList())) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
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
      AccountingReportType accountingReportType,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine)
      throws AxelorException {
    for (AccountingReportConfigLine column :
        accountingReportType.getAccountingReportConfigLineColumnList()) {
      if (!valuesMapByColumn.containsKey(column.getCode())) {
        valuesMapByColumn.put(column.getCode(), new HashMap<>());
      }

      for (AccountingReportConfigLine line :
          accountingReportType.getAccountingReportConfigLineList()) {
        if (!valuesMapByLine.containsKey(line.getCode())) {
          valuesMapByLine.put(line.getCode(), new HashMap<>());
        }

        if (!valuesMapByColumn.get(column.getCode()).containsKey(line.getCode())
            || valuesMapByColumn.get(column.getCode()).get(line.getCode()) == null) {
          this.createValue(accountingReport, column, line, valuesMapByColumn, valuesMapByLine);
        }
      }
    }

    return this.areAllValuesComputed(valuesMapByColumn);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine)
      throws AxelorException {
    if (column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE
        || line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE) {
      this.createReportValue(
          accountingReport, column, line, null, null, valuesMapByColumn, valuesMapByLine);
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      if (line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
      } else {
        this.createValueFromCustomRule(
            accountingReport,
            column,
            line,
            valuesMapByLine.get(line.getCode()),
            valuesMapByColumn,
            valuesMapByLine);
      }
    } else if (line.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      this.createValueFromCustomRule(
          accountingReport,
          column,
          line,
          valuesMapByColumn.get(column.getCode()),
          valuesMapByColumn,
          valuesMapByLine);
    } else {
      this.createValueFromMoveLines(
          accountingReport, column, line, valuesMapByColumn, valuesMapByLine);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromCustomRule(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine) {
    AccountingReportConfigLine configLine =
        column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
            ? column
            : line;
    Map<String, Object> contextMap = new HashMap<>();

    for (String code : valuesMap.keySet()) {
      if (valuesMap.get(code) != null) {
        contextMap.put(code, valuesMap.get(code).getResult());
      }
    }

    Context scriptContext = new Context(contextMap, Object.class);
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);
    BigDecimal result;

    try {
      result = (BigDecimal) scriptHelper.eval(configLine.getRule());
    } catch (Exception e) {
      valuesMapByColumn.get(column.getCode()).put(line.getCode(), null);
      valuesMapByLine.get(line.getCode()).put(column.getCode(), null);
      return;
    }

    this.createReportValue(
        accountingReport, column, line, result, null, valuesMapByColumn, valuesMapByLine);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createValueFromMoveLines(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine)
      throws AxelorException {
    if (!Objects.equals(column.getResultSelect(), line.getResultSelect())) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
    }

    Set<Account> accountSet = new HashSet<>(column.getAccountSet());
    accountSet.retainAll(line.getAccountSet());

    Set<AccountType> accountTypeSet = new HashSet<>(column.getAccountTypeSet());
    accountTypeSet.retainAll(line.getAccountTypeSet());

    List<MoveLine> moveLineResultList =
        moveLineRepo
            .all()
            .filter(this.buildQuery(accountingReport, accountSet, accountTypeSet))
            .bind("dateFrom", accountingReport.getDateFrom())
            .bind("dateTo", accountingReport.getDateTo())
            .bind("journal", accountingReport.getJournal())
            .bind("paymentMode", accountingReport.getPaymentMode())
            .bind("currency", accountingReport.getCurrency())
            .bind("company", accountingReport.getCompany())
            .bind("statusList", this.getMoveLineStatusList(accountingReport))
            .bind("accountSet", accountSet)
            .bind("accountTypeSet", accountTypeSet)
            .fetch();

    BigDecimal result =
        moveLineResultList.stream()
            .map(it -> this.getMoveLineAmount(it, column.getResultSelect()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    this.createReportValue(
        accountingReport, column, line, result, null, valuesMapByColumn, valuesMapByLine);
  }

  protected String buildQuery(
      AccountingReport accountingReport, Set<Account> accountSet, Set<AccountType> accountTypeSet) {
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

    if (CollectionUtils.isNotEmpty(accountSet)) {
      queryList.add("(self.account IS NULL OR self.account IN :accountSet)");
    }

    if (CollectionUtils.isNotEmpty(accountTypeSet)) {
      queryList.add("(self.account IS NULL OR self.account.accountType IN :accountTypeSet)");
    }

    return String.join(" AND ", queryList);
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
      BigDecimal result,
      BigDecimal previousResult,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine) {
    int columnNumber =
        accountingReport.getReportType().getAccountingReportConfigLineColumnList().indexOf(column);
    int lineNumber =
        accountingReport.getReportType().getAccountingReportConfigLineList().indexOf(line);

    AccountingReportValue accountingReportValue =
        new AccountingReportValue(
            columnNumber,
            lineNumber + lineOffset,
            result,
            previousResult,
            accountingReport,
            line,
            column);

    accountingReportValueRepo.save(accountingReportValue);

    valuesMapByColumn.get(column.getCode()).put(line.getCode(), accountingReportValue);
    valuesMapByLine.get(line.getCode()).put(column.getCode(), accountingReportValue);
  }
}
