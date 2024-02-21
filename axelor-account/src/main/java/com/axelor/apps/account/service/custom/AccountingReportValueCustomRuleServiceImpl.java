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

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportValue;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportValueRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.DateService;
import com.axelor.common.StringUtils;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AccountingReportValueCustomRuleServiceImpl extends AccountingReportValueAbstractService
    implements AccountingReportValueCustomRuleService {
  @Inject
  public AccountingReportValueCustomRuleServiceImpl(
      AccountRepository accountRepo,
      AccountingReportValueRepository accountingReportValueRepo,
      AnalyticAccountRepository analyticAccountRepo,
      DateService dateService) {
    super(accountRepo, accountingReportValueRepo, analyticAccountRepo, dateService);
  }

  @Override
  public void createValueFromCustomRuleForColumn(
      AccountingReport accountingReport,
      AccountingReportConfigLine groupColumn,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String parentTitle,
      LocalDate startDate,
      LocalDate endDate,
      int analyticCounter)
      throws AxelorException {
    if (accountingReport.getDisplayDetails()
        && line.getDetailBySelect() != AccountingReportConfigLineRepository.DETAIL_BY_NOTHING) {
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
            lineCode,
            analyticCounter);
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
          parentTitle,
          analyticCounter);
    }
  }

  @Override
  public void createValueFromCustomRule(
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
      int analyticCounter)
      throws AxelorException {
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
        line.getCode(),
        analyticCounter);
  }

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
      String lineCode,
      int analyticCounter)
      throws AxelorException {
    Map<String, AccountingReportValue> valuesMap =
        this.getValuesMap(
            column,
            line,
            groupColumn,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            parentTitle,
            lineCode);

    BigDecimal result =
        this.getResultFromCustomRule(
            accountingReport,
            column,
            line,
            groupColumn,
            valuesMap,
            valuesMapByColumn,
            valuesMapByLine,
            configAnalyticAccount,
            parentTitle,
            lineCode);

    if (result == null) {
      return;
    }

    String lineTitle = line.getLabel();
    if ((column.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
            && (line.getRuleTypeSelect()
                    != AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
                || column.getPriority() > line.getPriority()))
        || (groupColumn != null
            && groupColumn.getRuleTypeSelect()
                == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE)) {
      lineTitle =
          valuesMap.values().stream()
              .filter(Objects::nonNull)
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
        lineCode,
        analyticCounter);
  }

  protected Map<String, AccountingReportValue> getValuesMap(
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String parentTitle,
      String lineCode) {
    Map<String, AccountingReportValue> columnValues =
        valuesMapByColumn.get(
            this.getColumnCode(column.getCode(), parentTitle, groupColumn, configAnalyticAccount));
    Map<String, AccountingReportValue> lineValues = valuesMapByLine.get(lineCode);

    if (groupColumn != null
        && groupColumn.getRuleTypeSelect()
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return lineValues;
    } else if (column.getRuleTypeSelect()
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
        && line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      if (column.getPriority() > line.getPriority()) {
        return lineValues;
      } else {
        return columnValues;
      }
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return lineValues;
    } else {
      return columnValues;
    }
  }

  protected BigDecimal getResultFromCustomRule(
      AccountingReport accountingReport,
      AccountingReportConfigLine column,
      AccountingReportConfigLine line,
      AccountingReportConfigLine groupColumn,
      Map<String, AccountingReportValue> valuesMap,
      Map<String, Map<String, AccountingReportValue>> valuesMapByColumn,
      Map<String, Map<String, AccountingReportValue>> valuesMapByLine,
      AnalyticAccount configAnalyticAccount,
      String parentTitle,
      String lineCode) {
    String rule = this.getRule(column, line, groupColumn);
    Map<String, Object> contextMap =
        this.createRuleContextMap(
            column, line, groupColumn, valuesMap, configAnalyticAccount, parentTitle, rule);

    Context scriptContext = new Context(contextMap, Object.class);
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

    try {
      return (BigDecimal) scriptHelper.eval(rule);
    } catch (Exception e) {
      if (accountingReport.getTraceAnomalies()) {
        this.traceException(e, accountingReport, groupColumn, column, line);
      }

      this.addNullValue(
          column,
          groupColumn,
          valuesMapByColumn,
          valuesMapByLine,
          configAnalyticAccount,
          parentTitle,
          lineCode);

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
        if ((groupColumn != null
                && (groupColumn.getRuleTypeSelect()
                        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
                    || line.getRuleTypeSelect()
                        != AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE))
            || (!Strings.isNullOrEmpty(parentTitle)
                && column.getRuleTypeSelect()
                    == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE)
            || (configAnalyticAccount != null
                && (StringUtils.isEmpty(line.getRule()) || !line.getRule().equals(rule)))) {
          String[] tokens = code.split("__");

          if (tokens.length > 1) {
            if (groupColumn != null
                && groupColumn.getRuleTypeSelect()
                    == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
                && tokens[0].equals(column.getCode())) {
              contextMap.put(tokens[1], valuesMap.get(code).getResult());
            } else if ((groupColumn != null && tokens[1].equals(groupColumn.getCode()))
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
            == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE
        && line.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return column.getPriority() > line.getPriority() ? column.getRule() : line.getRule();
    } else if (column.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_CUSTOM_RULE) {
      return column.getRule();
    } else {
      return line.getRule();
    }
  }
}
