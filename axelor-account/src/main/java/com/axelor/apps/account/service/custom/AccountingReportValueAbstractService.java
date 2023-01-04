package com.axelor.apps.account.service.custom;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.axelor.db.Query;
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
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public abstract class AccountingReportValueAbstractService {
  protected AccountingReportValueRepository accountingReportValueRepo;
  protected AnalyticAccountRepository analyticAccountRepo;

  @Inject
  public AccountingReportValueAbstractService(
      AccountingReportValueRepository accountingReportValueRepo,
      AnalyticAccountRepository analyticAccountRepo) {
    this.accountingReportValueRepo = accountingReportValueRepo;
    this.analyticAccountRepo = analyticAccountRepo;
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
      String lineCode,
      int analyticCounter) {
    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
}
