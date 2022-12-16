package com.axelor.apps.account.service.custom;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportValuePercentageServiceImpl extends AccountingReportValueAbstractService
    implements AccountingReportValuePercentageService {
  @Inject
  public AccountingReportValuePercentageServiceImpl(AccountingReportValueRepository accountingReportValueRepo) {
    super(accountingReportValueRepo);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createPercentageValue(
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
      int analyticCounter) {
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
          parentTitle,
          analyticCounter);
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
      String parentTitle,
      int analyticCounter) {
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
          result,
          analyticCounter);
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
      BigDecimal result,
      int analyticCounter) {
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
        result.abs(),
        valuesMapByColumn,
        valuesMapByLine,
        configAnalyticAccount,
        lineCode,
        analyticCounter);
  }
}
