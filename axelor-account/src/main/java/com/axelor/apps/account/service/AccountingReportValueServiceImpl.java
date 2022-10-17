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
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportValueServiceImpl implements AccountingReportValueService {
  protected AccountingReportValueRepository accountingReportValueRepo;
  protected MoveLineRepository moveLineRepo;

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeReportValues(AccountingReport accountingReport) throws AxelorException {
    AccountingReportType accountingReportType = accountingReport.getReportType();
    Map<String, Map<String, AccountingReportValue>> valuesMapByColumn = new HashMap<>();
    Map<String, Map<String, AccountingReportValue>> valuesMapByLine = new HashMap<>();

    this.checkAccountingReportType(accountingReportType);

    while (!this.areAllValuesComputed(valuesMapByColumn)) {
      this.createReportValues(
          accountingReport, accountingReportType, valuesMapByColumn, valuesMapByLine);
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
  protected void createReportValues(
      AccountingReport accountingReport,
      AccountingReportType accountingReportType,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine)
      throws AxelorException {
    for (AccountingReportConfigLine column :
        accountingReportType.getAccountingReportConfigLineColumnList()) {
      valuesMapByColumn.put(column.getCode(), new HashMap<>());

      for (AccountingReportConfigLine line :
          accountingReportType.getAccountingReportConfigLineList()) {
        valuesMapByLine.put(line.getCode(), new HashMap<>());

        this.fillReportValue(accountingReport, column, line, valuesMapByColumn, valuesMapByLine);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void fillReportValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine)
      throws AxelorException {
    AccountingReportValue value =
        this.getValue(accountingReport, column, line, valuesMapByColumn, valuesMapByLine);

    valuesMapByColumn.get(column.getCode()).put(line.getCode(), value);
    valuesMapByLine.get(line.getCode()).put(column.getCode(), value);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected AccountingReportValue getValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine)
      throws AxelorException {
    if (column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE
        || line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE) {
      return this.createReportValue(accountingReport, column, line, null, null);
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      if (line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
      } else {
        return this.getValueFromCustomRule(
            accountingReport, column, line, valuesMapByLine.get(line.getCode()));
      }
    } else if (line.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return this.getValueFromCustomRule(
          accountingReport, column, line, valuesMapByColumn.get(column.getCode()));
    } else {
      return this.getValueFromMoveLines(accountingReport, column, line);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected AccountingReportValue getValueFromCustomRule(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, AccountingReportValue> valuesMap) {
    AccountingReportConfigLine configLine =
        column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
            ? column
            : line;
    Map<String, Object> contextMap = new HashMap<>();

    for (String code : valuesMap.keySet()) {
      contextMap.put(code, valuesMap.get(code).getResult());
    }

    Context scriptContext = new Context(contextMap, Object.class);
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);
    BigDecimal result = (BigDecimal) scriptHelper.eval(configLine.getRule());

    if (result == null) {
      return null;
    }

    return this.createReportValue(accountingReport, column, line, result, null);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected AccountingReportValue getValueFromMoveLines(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line)
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
            .filter(
                "(self.date IS NULL OR :dateFrom IS NULL OR self.date >= :dateFrom) "
                    + "AND (self.date IS NULL OR :dateTo IS NULL OR self.date <= :dateTo) "
                    + "AND (self.move.journal IS NULL OR :journal IS NULL OR self.move.journal = :journal) "
                    + "AND (self.move.paymentMode IS NULL OR :paymentMode IS NULL OR self.move.paymentMode = :paymentMode) "
                    + "AND (self.move.currency IS NULL OR :currency IS NULL OR self.move.currency = :currency) "
                    + "AND (self.move.company IS NULL OR :company IS NULL OR self.move.company = :company) "
                    + "AND self.move.statusSelect IN :statusList "
                    + "AND (self.account IS NULL OR :accountSet IS NULL OR self.account IN :accountSet) "
                    + "AND (self.account IS NULL OR :accountType IS NULL OR self.account.accountType IN :accountTypeSet)")
            .bind("dateFrom", accountingReport.getDateFrom())
            .bind("dateTo", accountingReport.getDateTo())
            .bind("journal", accountingReport.getJournal())
            .bind("paymentMode", accountingReport.getPaymentMode())
            .bind("currency", accountingReport.getCurrency())
            .bind("company", accountingReport.getCompany())
            .bind("statusList", this.getMoveLineStatusList(accountingReport))
            .bind("accountSet", accountSet.isEmpty() ? null : accountSet)
            .bind("accountTypeSet", accountTypeSet.isEmpty() ? null : accountTypeSet)
            .fetch();

    BigDecimal result =
        moveLineResultList.stream()
            .map(it -> this.getMoveLineAmount(it, column.getResultSelect()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    return this.createReportValue(accountingReport, column, line, result, null);
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
  protected AccountingReportValue createReportValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      BigDecimal result,
      BigDecimal previousResult) {
    int columnNumber =
        accountingReport.getReportType().getAccountingReportConfigLineColumnList().indexOf(column);
    int lineNumber =
        accountingReport.getReportType().getAccountingReportConfigLineList().indexOf(line);

    AccountingReportValue accountingReportValue =
        new AccountingReportValue(
            columnNumber, lineNumber, result, previousResult, accountingReport, line, column);

    return accountingReportValueRepo.save(accountingReportValue);
  }
}
