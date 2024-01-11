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
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigDecimal;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class AccountingReportValueMoveLineServiceImpl extends AccountingReportValueAbstractService
    implements AccountingReportValueMoveLineService {
  protected MoveLineRepository moveLineRepo;
  protected Set<AnalyticAccount> groupColumnAnalyticAccountSet;
  protected Set<AnalyticAccount> columnAnalyticAccountSet;
  protected Set<AnalyticAccount> lineAnalyticAccountSet;

  @Inject
  public AccountingReportValueMoveLineServiceImpl(
      AccountRepository accountRepository,
      AccountingReportValueRepository accountingReportValueRepo,
      AnalyticAccountRepository analyticAccountRepo,
      MoveLineRepository moveLineRepo,
      DateService dateService) {
    super(accountRepository, accountingReportValueRepo, analyticAccountRepo, dateService);
    this.moveLineRepo = moveLineRepo;
  }

  @Override
  public void createValueFromMoveLines(
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
    this.checkResultSelects(accountingReport, groupColumn, column, line);

    if (accountingReport.getDisplayDetails()
        && line.getDetailBySelect() == AccountingReportConfigLineRepository.DETAIL_BY_ACCOUNT) {
      int counter = 1;

      Set<Account> detailByAccountSet = new HashSet<>(line.getAccountSet());

      if (CollectionUtils.isNotEmpty(line.getAccountTypeSet())) {
        detailByAccountSet =
            this.mergeWithAccountTypes(detailByAccountSet, line.getAccountTypeSet());
      }

      if (StringUtils.notEmpty(line.getAccountCode())) {
        detailByAccountSet = this.mergeWithAccountCode(detailByAccountSet, line.getAccountCode());
      }

      detailByAccountSet =
          this.sortSet(detailByAccountSet, Comparator.comparing(Account::getLabel));

      for (Account account : detailByAccountSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);

        if (!valuesMapByLine.containsKey(lineCode)) {
          valuesMapByLine.put(lineCode, new HashMap<>());
        }

        accountingReport = this.fetchAccountingReport(accountingReport);

        account = JPA.find(Account.class, account.getId());
        line = JPA.find(AccountingReportConfigLine.class, line.getId());
        column = JPA.find(AccountingReportConfigLine.class, column.getId());
        groupColumn =
            groupColumn != null
                ? JPA.find(AccountingReportConfigLine.class, groupColumn.getId())
                : null;
        configAnalyticAccount =
            configAnalyticAccount != null
                ? JPA.find(AnalyticAccount.class, configAnalyticAccount.getId())
                : null;

        this.mergeSetsAndCreateValueFromMoveLines(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            groupAccount,
            configAnalyticAccount,
            account,
            null,
            null,
            parentTitle,
            account.getLabel(),
            lineCode,
            startDate,
            endDate,
            analyticCounter);

        JPA.clear();
        AccountingReportValueServiceImpl.incrementLineOffset();
      }
    } else if (accountingReport.getDisplayDetails()
        && line.getDetailBySelect()
            == AccountingReportConfigLineRepository.DETAIL_BY_ACCOUNT_TYPE) {
      int counter = 1;

      Set<AccountType> detailByAccountTypeSet = new HashSet<>(line.getAccountTypeSet());

      if (CollectionUtils.isNotEmpty(line.getAccountSet())) {
        detailByAccountTypeSet =
            this.mergeWithAccounts(detailByAccountTypeSet, line.getAccountSet());
      }

      detailByAccountTypeSet =
          this.sortSet(detailByAccountTypeSet, Comparator.comparing(AccountType::getName));

      for (AccountType accountType : detailByAccountTypeSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);

        if (!valuesMapByLine.containsKey(lineCode)) {
          valuesMapByLine.put(lineCode, new HashMap<>());
        }

        accountingReport = this.fetchAccountingReport(accountingReport);

        accountType = JPA.find(AccountType.class, accountType.getId());
        line = JPA.find(AccountingReportConfigLine.class, line.getId());
        column = JPA.find(AccountingReportConfigLine.class, column.getId());
        groupColumn =
            groupColumn != null
                ? JPA.find(AccountingReportConfigLine.class, groupColumn.getId())
                : null;
        configAnalyticAccount =
            configAnalyticAccount != null
                ? JPA.find(AnalyticAccount.class, configAnalyticAccount.getId())
                : null;

        this.mergeSetsAndCreateValueFromMoveLines(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            groupAccount,
            configAnalyticAccount,
            null,
            accountType,
            null,
            parentTitle,
            accountType.getName(),
            lineCode,
            startDate,
            endDate,
            analyticCounter);

        JPA.clear();
        AccountingReportValueServiceImpl.incrementLineOffset();
      }
    } else if (accountingReport.getDisplayDetails()
        && line.getDetailBySelect()
            == AccountingReportConfigLineRepository.DETAIL_BY_ANALYTIC_ACCOUNT) {
      int counter = 1;
      Set<AnalyticAccount> sortedAnalyticAccountSet =
          this.sortSet(
              line.getAnalyticAccountSet(), Comparator.comparing(AnalyticAccount::getFullName));

      for (AnalyticAccount analyticAccount : sortedAnalyticAccountSet) {
        String lineCode = String.format("%s_%d", line.getCode(), counter++);

        if (!valuesMapByLine.containsKey(lineCode)) {
          valuesMapByLine.put(lineCode, new HashMap<>());
        }

        accountingReport = this.fetchAccountingReport(accountingReport);

        analyticAccount = JPA.find(AnalyticAccount.class, analyticAccount.getId());
        line = JPA.find(AccountingReportConfigLine.class, line.getId());
        column = JPA.find(AccountingReportConfigLine.class, column.getId());
        groupColumn =
            groupColumn != null
                ? JPA.find(AccountingReportConfigLine.class, groupColumn.getId())
                : null;
        configAnalyticAccount =
            configAnalyticAccount != null
                ? JPA.find(AnalyticAccount.class, configAnalyticAccount.getId())
                : null;

        this.mergeSetsAndCreateValueFromMoveLines(
            accountingReport,
            groupColumn,
            column,
            line,
            valuesMapByColumn,
            valuesMapByLine,
            groupAccount,
            configAnalyticAccount,
            null,
            null,
            analyticAccount,
            parentTitle,
            analyticAccount.getFullName(),
            lineCode,
            startDate,
            endDate,
            analyticCounter);

        JPA.clear();
        AccountingReportValueServiceImpl.incrementLineOffset();
      }
    } else {
      this.mergeSetsAndCreateValueFromMoveLines(
          accountingReport,
          groupColumn,
          column,
          line,
          valuesMapByColumn,
          valuesMapByLine,
          groupAccount,
          configAnalyticAccount,
          null,
          null,
          null,
          parentTitle,
          line.getLabel(),
          line.getCode(),
          startDate,
          endDate,
          analyticCounter);
    }
  }

  protected <T extends AuditableModel> Set<T> sortSet(Set<T> set, Comparator<T> comparator) {
    Set<T> sortedSet = new TreeSet<>(comparator);
    sortedSet.addAll(set);

    return sortedSet;
  }

  protected void mergeSetsAndCreateValueFromMoveLines(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      Account groupAccount,
      AnalyticAccount configAnalyticAccount,
      Account detailByAccount,
      AccountType detailByAccountType,
      AnalyticAccount detailByAnalyticAccount,
      String parentTitle,
      String lineTitle,
      String lineCode,
      LocalDate startDate,
      LocalDate endDate,
      int analyticCounter)
      throws AxelorException {
    Set<Account> lineAccountSet = line.getAccountSet();
    Set<AccountType> lineAccountTypeSet = line.getAccountTypeSet();
    Set<AnalyticAccount> lineAnalyticAccountSet = line.getAnalyticAccountSet();

    if (detailByAccount != null) {
      lineAccountSet = new HashSet<>(Collections.singletonList(detailByAccount));
      lineAccountTypeSet = new HashSet<>();
    }

    if (detailByAccountType != null) {
      lineAccountTypeSet = new HashSet<>(Collections.singletonList(detailByAccountType));
    }

    if (detailByAnalyticAccount != null) {
      lineAnalyticAccountSet = new HashSet<>(Collections.singletonList(detailByAnalyticAccount));
    }

    Set<Account> accountSet;
    Set<AccountType> accountTypeSet = null;
    Set<AnalyticAccount> analyticAccountSet =
        this.mergeSets(column.getAnalyticAccountSet(), lineAnalyticAccountSet);

    if (groupAccount != null) {
      accountSet = new HashSet<>(Collections.singletonList(groupAccount));
    } else {
      accountSet = this.mergeSets(column.getAccountSet(), lineAccountSet);
      accountTypeSet = this.mergeSets(column.getAccountTypeSet(), lineAccountTypeSet);
    }

    if (groupColumn != null && groupAccount == null) {
      accountSet = this.mergeSets(groupColumn.getAccountSet(), accountSet);
      accountTypeSet = this.mergeSets(groupColumn.getAccountTypeSet(), accountTypeSet);
      analyticAccountSet = this.mergeSets(groupColumn.getAnalyticAccountSet(), analyticAccountSet);
    }

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
        lineTitle,
        lineCode,
        analyticCounter);
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

  protected Set<Account> mergeWithAccountTypes(
      Set<Account> accountSet, Set<AccountType> accountTypeSet) {
    Set<Account> tempSet =
        this.getSetFromStream(
            accountTypeSet.stream(),
            accountType -> accountRepo.findByAccountType(accountType).fetch());

    return this.mergeSets(accountSet, tempSet);
  }

  protected Set<Account> mergeWithAccountCode(Set<Account> accountSet, String accountCode) {
    Set<Account> tempSet =
        this.getSetFromStream(
            Arrays.stream(accountCode.split(",")),
            accountCodeToken ->
                accountRepo.all().filter("self.code LIKE ?", accountCodeToken).fetch());

    return this.mergeSets(accountSet, tempSet);
  }

  protected <T> Set<Account> getSetFromStream(
      Stream<T> stream, Function<T, List<Account>> function) {
    return stream.map(function).flatMap(Collection::stream).collect(Collectors.toSet());
  }

  protected Set<AccountType> mergeWithAccounts(
      Set<AccountType> accountTypeSet, Set<Account> accountSet) {
    Set<AccountType> tempSet =
        accountSet.stream().map(Account::getAccountType).collect(Collectors.toSet());

    return this.mergeSets(accountTypeSet, tempSet);
  }

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
      String lineCode,
      int analyticCounter)
      throws AxelorException {
    Set<AnalyticAccount> resultAnalyticAccountSet =
        this.mergeSets(
            analyticAccountSet,
            configAnalyticAccount == null
                ? null
                : new HashSet<>(Collections.singletonList(configAnalyticAccount)));

    List<MoveLine> moveLineList =
        this.getMoveLineQuery(
                accountingReport,
                groupColumn,
                column,
                line,
                accountSet,
                accountTypeSet,
                resultAnalyticAccountSet,
                startDate,
                endDate)
            .fetch();

    BigDecimal result =
        this.getResultFromMoveLine(
            accountingReport,
            groupColumn,
            column,
            line,
            moveLineList,
            resultAnalyticAccountSet,
            startDate,
            endDate,
            this.getResultSelect(column, line, groupColumn));

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
        lineCode,
        analyticCounter);
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
    Pair<LocalDate, LocalDate> dates =
        this.getDates(accountingReport, groupColumn, column, startDate, endDate);

    return this.buildMoveLineQuery(
        accountingReport,
        accountSet,
        accountTypeSet,
        analyticAccountSet,
        groupColumn,
        column,
        line,
        dates.getLeft(),
        dates.getRight());
  }

  protected Pair<LocalDate, LocalDate> getDates(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      LocalDate startDate,
      LocalDate endDate) {
    if (column.getComputePreviousYear()) {
      return Pair.of(startDate.minusYears(1), endDate.minusYears(1));
    } else if (this.isComputeOnOtherPeriod(groupColumn, column)) {
      return Pair.of(accountingReport.getOtherDateFrom(), accountingReport.getOtherDateTo());
    } else {
      return Pair.of(startDate, endDate);
    }
  }

  protected boolean isComputeOnOtherPeriod(
      AccountingReportConfigLine groupColumn, AccountingReportConfigLine column) {
    return column.getComputeOtherPeriod()
        || (groupColumn != null && groupColumn.getComputeOtherPeriod());
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

    this.addDateQueries(queryList, accountingReport);

    if (accountingReport.getJournal() != null) {
      queryList.add("(self.move.journal IS NULL OR self.move.journal = :journal)");
    }

    if (accountingReport.getPaymentMode() != null) {
      queryList.add("(self.move.paymentMode IS NULL OR self.move.paymentMode = :paymentMode)");
    }

    if (accountingReport.getCurrency() != null) {
      queryList.add("(self.move.currency IS NULL OR self.move.currency = :currency)");
    }

    if (accountingReport.getCompany() != null) {
      queryList.add("(self.move.company IS NULL OR self.move.company = :company)");
    }

    queryList.addAll(
        this.getAccountFilters(
            accountSet,
            accountTypeSet,
            groupColumn == null ? null : groupColumn.getAccountCode(),
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

  protected void addDateQueries(List<String> queryList, AccountingReport accountingReport) {
    if (accountingReport.getDateFrom() != null) {
      queryList.add("(self.date IS NULL OR self.date >= :dateFrom)");
    }

    if (accountingReport.getDateTo() != null) {
      queryList.add("(self.date IS NULL OR self.date <= :dateTo)");
    }
  }

  protected BigDecimal getResultFromMoveLine(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      List<MoveLine> moveLineList,
      Set<AnalyticAccount> analyticAccountSet,
      LocalDate startDate,
      LocalDate endDate,
      int resultSelect) {
    String groupColumnAnalyticAccountCode =
        groupColumn != null ? groupColumn.getAnalyticAccountCode() : null;
    String columnAnalyticAccountCode = column.getAnalyticAccountCode();
    String lineAnalyticAccountCode = line.getAnalyticAccountCode();
    groupColumnAnalyticAccountSet =
        groupColumn != null && StringUtils.notEmpty(groupColumnAnalyticAccountCode)
            ? this.fetchAnalyticAccountsFromCode(groupColumnAnalyticAccountCode)
            : new HashSet<>();
    columnAnalyticAccountSet =
        StringUtils.notEmpty(columnAnalyticAccountCode)
            ? this.fetchAnalyticAccountsFromCode(columnAnalyticAccountCode)
            : new HashSet<>();
    lineAnalyticAccountSet =
        StringUtils.notEmpty(lineAnalyticAccountCode)
            ? this.fetchAnalyticAccountsFromCode(lineAnalyticAccountCode)
            : new HashSet<>();
    return moveLineList.stream()
        .map(
            it ->
                this.getMoveLineAmount(
                    it,
                    accountingReport,
                    groupColumn,
                    column,
                    line,
                    analyticAccountSet,
                    startDate,
                    endDate,
                    resultSelect))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  protected List<Integer> getMoveLineStatusList(AccountingReport accountingReport) {
    List<Integer> statusList =
        new ArrayList<>(
            Arrays.asList(MoveRepository.STATUS_DAYBOOK, MoveRepository.STATUS_ACCOUNTED));

    if (accountingReport.getDisplaySimulatedMove()) {
      statusList.add(MoveRepository.STATUS_SIMULATED);
    }

    return statusList;
  }

  protected BigDecimal getMoveLineAmount(
      MoveLine moveLine,
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Set<AnalyticAccount> analyticAccountSet,
      LocalDate startDate,
      LocalDate endDate,
      int resultSelect) {
    String groupColumnAnalyticAccountCode =
        groupColumn == null ? null : groupColumn.getAnalyticAccountCode();

    if (CollectionUtils.isNotEmpty(analyticAccountSet)
        || StringUtils.notEmpty(groupColumnAnalyticAccountCode)
        || StringUtils.notEmpty(column.getAnalyticAccountCode())
        || StringUtils.notEmpty(line.getAnalyticAccountCode())) {
      return this.getAnalyticAmount(
          moveLine, analyticAccountSet, resultSelect, moveLine.getDebit().signum() > 0);
    }

    BigDecimal value = moveLine.getDebit().subtract(moveLine.getCredit());

    return resultSelect == AccountingReportConfigLineRepository.RESULT_DEBIT_MINUS_CREDIT
        ? value
        : value.negate();
  }

  protected BigDecimal getAnalyticAmount(
      MoveLine moveLine,
      Set<AnalyticAccount> analyticAccountSet,
      int resultSelect,
      boolean isDebit) {
    if (CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
      return BigDecimal.ZERO;
    }

    BigDecimal value =
        moveLine.getAnalyticMoveLineList().stream()
            .filter(it -> this.containsAnalyticAccount(it.getAnalyticAccount(), analyticAccountSet))
            .map(AnalyticMoveLine::getAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    if (resultSelect == AccountingReportConfigLineRepository.RESULT_DEBIT_MINUS_CREDIT) {
      return isDebit ? value : value.negate();
    } else {
      return isDebit ? value.negate() : value;
    }
  }

  protected boolean containsAnalyticAccount(
      AnalyticAccount analyticAccount, Set<AnalyticAccount> analyticAccountSet) {
    return (CollectionUtils.isNotEmpty(analyticAccountSet)
            && analyticAccountSet.contains(analyticAccount))
        || groupColumnAnalyticAccountSet.contains(analyticAccount)
        || columnAnalyticAccountSet.contains(analyticAccount)
        || lineAnalyticAccountSet.contains(analyticAccount);
  }
}
