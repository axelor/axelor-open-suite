package com.axelor.apps.account.service;

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
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportValueServiceImpl {
  protected AccountingReportValueRepository accountingReportValueRepo;
  protected MoveLineRepository moveLineRepo;

  @Transactional(rollbackOn = {Exception.class})
  public void computeReportValues(AccountingReport accountingReport) throws AxelorException {
    AccountingReportType accountingReportType = accountingReport.getReportType();

    this.checkAccountingReportType(accountingReportType);
    this.initAccountingReportTypeLinesAndColumns(accountingReportType);

    while (!this.areAllValuesComputed(accountingReportType)) {
      this.createReportValues(accountingReport, accountingReportType);
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

  protected void initAccountingReportTypeLinesAndColumns(
      AccountingReportType accountingReportType) {
    accountingReportType.getAccountingReportConfigLineList().forEach(it -> it.setComputed(true));
    accountingReportType
        .getAccountingReportConfigLineColumnList()
        .forEach(it -> it.setComputed(true));
  }

  protected boolean areAllValuesComputed(AccountingReportType accountingReportType) {
    return accountingReportType.getAccountingReportConfigLineList().stream()
            .allMatch(AccountingReportConfigLine::getComputed)
        && accountingReportType.getAccountingReportConfigLineColumnList().stream()
            .allMatch(AccountingReportConfigLine::getComputed);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createReportValues(
      AccountingReport accountingReport, AccountingReportType accountingReportType) {
    Map<String, Map<String, AccountingReportValue>> valuesMapByColumn = new HashMap<>();
    Map<String, Map<String, AccountingReportValue>> valuesMapByLine = new HashMap<>();

    for (AccountingReportConfigLine column :
        accountingReportType.getAccountingReportConfigLineColumnList()) {
      valuesMapByColumn.put(column.getCode(), new HashMap<>());

      for (AccountingReportConfigLine line :
          accountingReportType.getAccountingReportConfigLineList()) {
        valuesMapByLine.put(line.getCode(), new HashMap<>());

        this.createReportValue(accountingReport, column, line, valuesMapByColumn, valuesMapByLine);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createReportValue(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine) {}

  protected BigDecimal getResult(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line)
      throws AxelorException {
    if (column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE
        || line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_NO_VALUE) {
      return null;
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      if (line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
      } else {
        return this.getResultFromCustomRule();
      }
    } else if (line.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return this.getResultFromCustomRule();
    } else {
      return this.getResultFromMoveLines(accountingReport, column, line);
    }
  }

  protected BigDecimal getResultFromCustomRule() {
    return null;
  }

  protected BigDecimal getResultFromMoveLines(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line)
      throws AxelorException {
    if (!Objects.equals(column.getResultSelect(), line.getResultSelect())) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "");
    }

    List<MoveLine> moveLineResultList =
        moveLineRepo
            .all()
            .filter(
                "self.date >= :dateFrom AND self.date <= :dateTo AND self.move.journal = :journal "
                    + "AND self.move.paymentMode = :paymentMode AND self.move.currency = :currency "
                    + "AND self.move.company = :company AND self.move.statusSelect IN :statusList")
            .bind("dateFrom", accountingReport.getDateFrom())
            .bind("dateTo", accountingReport.getDateTo())
            .bind("journal", accountingReport.getJournal())
            .bind("paymentMode", accountingReport.getPaymentMode())
            .bind("currency", accountingReport.getCurrency())
            .bind("company", accountingReport.getCompany())
            .bind("statusList", this.getMoveLineStatusList(accountingReport))
            .fetch();

    return moveLineResultList.stream()
        .map(it -> this.getMoveLineAmount(it, column.getResultSelect()))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
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
      int columnNumber,
      int lineNumber,
      BigDecimal result,
      BigDecimal previousResult) {
    AccountingReportValue accountingReportValue =
        new AccountingReportValue(
            columnNumber, lineNumber, result, previousResult, accountingReport, line, column);

    accountingReportValueRepo.save(accountingReportValue);
  }
}
