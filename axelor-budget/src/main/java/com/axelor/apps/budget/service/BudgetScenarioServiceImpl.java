/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.repo.BudgetScenarioRepository;
import com.axelor.apps.budget.db.repo.BudgetScenarioVariableRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BudgetScenarioServiceImpl implements BudgetScenarioService {

  protected BudgetScenarioVariableRepository budgetScenarioVariableRepository;
  protected BudgetScenarioRepository budgetScenarioRepository;
  protected CurrencyScaleServiceBudget currencyScaleServiceBudget;

  @Inject
  public BudgetScenarioServiceImpl(
      BudgetScenarioVariableRepository budgetScenarioVariableRepository,
      BudgetScenarioRepository budgetScenarioRepository,
      CurrencyScaleServiceBudget currencyScaleServiceBudget) {
    this.budgetScenarioVariableRepository = budgetScenarioVariableRepository;
    this.budgetScenarioRepository = budgetScenarioRepository;
    this.currencyScaleServiceBudget = currencyScaleServiceBudget;
  }

  @Override
  public Map<String, Object> buildVariableMap(BudgetScenario budgetScenario, int yearNumber)
      throws AxelorException {
    Map<String, Object> variableAmountMap = new HashMap<>();
    if (budgetScenario != null
        && !ObjectUtils.isEmpty(budgetScenario.getBudgetScenarioLineList())) {
      for (BudgetScenarioLine budgetScenarioLine : budgetScenario.getBudgetScenarioLineList()) {
        BigDecimal yearValue =
            currencyScaleServiceBudget.getCompanyScaledValue(
                budgetScenario, getYearValue(budgetScenarioLine, yearNumber));
        if (!variableAmountMap.containsKey(
            budgetScenarioLine.getBudgetScenarioVariable().getCode())) {
          variableAmountMap.put(
              budgetScenarioLine.getBudgetScenarioVariable().getCode(), yearValue);
        } else {
          variableAmountMap.replace(
              budgetScenarioLine.getBudgetScenarioVariable().getCode(),
              ((BigDecimal)
                      variableAmountMap.get(
                          budgetScenarioLine.getBudgetScenarioVariable().getCode()))
                  .add(yearValue));
        }
      }
    }
    computeFormulaVariable(variableAmountMap, yearNumber, budgetScenario);

    return variableAmountMap;
  }

  @Override
  public Map<String, Object> getVariableMap(BudgetScenario budgetScenario, int yearNumber)
      throws AxelorException {
    Map<String, Object> variableAmountMap = new HashMap<>();
    if (budgetScenario == null || ObjectUtils.isEmpty(budgetScenario.getBudgetScenarioLineList())) {
      return variableAmountMap;
    }

    for (BudgetScenarioLine budgetScenarioLine : budgetScenario.getBudgetScenarioLineList()) {
      BigDecimal yearValue =
          currencyScaleServiceBudget.getCompanyScaledValue(
              budgetScenario, getYearValue(budgetScenarioLine, yearNumber));
      if (!variableAmountMap.containsKey(
          budgetScenarioLine.getBudgetScenarioVariable().getCode())) {
        variableAmountMap.put(budgetScenarioLine.getBudgetScenarioVariable().getCode(), yearValue);
      } else {
        variableAmountMap.replace(
            budgetScenarioLine.getBudgetScenarioVariable().getCode(),
            ((BigDecimal)
                    variableAmountMap.get(budgetScenarioLine.getBudgetScenarioVariable().getCode()))
                .add(yearValue));
      }
    }
    return variableAmountMap;
  }

  protected BigDecimal getYearValue(BudgetScenarioLine budgetScenarioLine, int yearNumber) {
    switch (yearNumber) {
      case 2:
        return budgetScenarioLine.getYear2Value();
      case 3:
        return budgetScenarioLine.getYear3Value();
      case 4:
        return budgetScenarioLine.getYear4Value();
      case 5:
        return budgetScenarioLine.getYear5Value();
      case 6:
        return budgetScenarioLine.getYear6Value();
      default:
        return budgetScenarioLine.getYear1Value();
    }
  }

  protected Map<String, Object> computeFormulaVariable(
      Map<String, Object> variableAmountMap, int yearNumber, BudgetScenario budgetScenario)
      throws AxelorException {
    List<BudgetScenarioVariable> variableList =
        budgetScenarioVariableRepository
            .all()
            .filter(
                "self.entryMethod = ?1 AND self.formula IS NOT NULL",
                BudgetScenarioVariableRepository
                    .BUDGET_SCENARIO_VARIABLE_ENTRY_METHOD_TYPE_SELECT_FORMULA)
            .order("id")
            .fetch();
    if (ObjectUtils.isEmpty(variableList)) {
      return variableAmountMap;
    } else {
      fillVariableAmountMapWithFormula(variableAmountMap, variableList, yearNumber, budgetScenario);
      replaceNullValuesInVariableMap(variableAmountMap);
    }
    return variableAmountMap;
  }

  protected void fillVariableAmountMapWithFormula(
      Map<String, Object> variableAmountMap,
      List<BudgetScenarioVariable> variableList,
      int yearNumber,
      BudgetScenario budgetScenario)
      throws AxelorException {
    for (BudgetScenarioVariable variable : variableList) {
      variableAmountMap.put(variable.getCode(), null);
    }
    int nullCount = -1;
    int previousNullCount;

    if (yearNumber == 1) {
      checkErrorsOnVariableAmountMap(variableAmountMap, budgetScenario);
    }

    while (nullCount != 0) {
      previousNullCount = nullCount;

      nullCount = this.fillAllMap(variableAmountMap);

      if (nullCount == previousNullCount) {
        return;
      }
    }
  }

  protected void checkErrorsOnVariableAmountMap(
      Map<String, Object> variableAmountMap, BudgetScenario budgetScenario) {
    Map<String, Object> copyVariableAmountMap = new HashMap<>();

    for (String lineCode : variableAmountMap.keySet().stream().collect(Collectors.toList())) {
      if (variableAmountMap.get(lineCode) != null) {
        copyVariableAmountMap.put(lineCode, variableAmountMap.get(lineCode));
      } else {
        copyVariableAmountMap.put(lineCode, BigDecimal.ZERO);
      }
    }

    traceErrorsOnScenarioMap(copyVariableAmountMap, budgetScenario);
  }

  protected void traceErrorsOnScenarioMap(
      Map<String, Object> variableAmountMap, BudgetScenario budgetScenario) {
    for (String lineCode : variableAmountMap.keySet().stream().collect(Collectors.toList())) {

      BudgetScenarioVariable variable = budgetScenarioVariableRepository.findByCode(lineCode);
      if (variable != null
          && variable.getEntryMethod()
              == BudgetScenarioVariableRepository
                  .BUDGET_SCENARIO_VARIABLE_ENTRY_METHOD_TYPE_SELECT_FORMULA
          && variable.getEntryMethod() != null) {
        Context scriptContext = new Context(variableAmountMap, Object.class);
        ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

        try {
          Object result = scriptHelper.eval(variable.getFormula());
          if (result == null) {
            AxelorException exception =
                new AxelorException(
                    new Throwable(String.format("No such field in: %s", variable.getFormula())),
                    budgetScenario,
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    String.format(I18n.get(BudgetExceptionMessage.BUDGET_VARIABLE), lineCode));
            TraceBackService.trace(exception);
          }
        } catch (Exception e) {
          TraceBackService.trace(
              new AxelorException(
                  e.getCause(),
                  budgetScenario,
                  TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                  String.format(I18n.get(BudgetExceptionMessage.BUDGET_VARIABLE), lineCode)));
        }
      }
    }
  }

  protected int fillAllMap(Map<String, Object> variableAmountMap) throws AxelorException {
    if (ObjectUtils.isEmpty(variableAmountMap)) {
      return 0;
    }
    int nullCount = 0;

    for (String lineCode : variableAmountMap.keySet().stream().collect(Collectors.toList())) {
      if (variableAmountMap.get(lineCode) == null) {

        BudgetScenarioVariable variable = budgetScenarioVariableRepository.findByCode(lineCode);
        Context scriptContext = new Context(variableAmountMap, Object.class);
        ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

        try {
          variableAmountMap.replace(lineCode, scriptHelper.eval(variable.getFormula()));
        } catch (Exception e) {
          // Exceptions will be throwed in traceErrorsOnScenarioMap and will be traced
        }

        if (variableAmountMap.get(lineCode) == null) {
          nullCount++;
        }
      }
    }

    return nullCount;
  }

  protected void replaceNullValuesInVariableMap(Map<String, Object> variableAmountMap) {
    if (ObjectUtils.isEmpty(variableAmountMap)) {
      return;
    }
    variableAmountMap =
        variableAmountMap.entrySet().stream()
            .map(
                entry -> {
                  if (entry.getValue() == null) entry.setValue(BigDecimal.ZERO);
                  return entry;
                })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateScenario(BudgetScenario budgetScenario) throws AxelorException {
    generateFormulaScenarioLines(budgetScenario);
    budgetScenario.setStatusSelect(BudgetScenarioRepository.BUDGET_SCENARIO_STATUS_SELECT_VALID);
  }

  protected void generateFormulaScenarioLines(BudgetScenario budgetScenario)
      throws AxelorException {
    if (ObjectUtils.isEmpty(budgetScenario.getBudgetScenarioLineList())
        || ObjectUtils.isEmpty(budgetScenario.getYearSet())) {
      return;
    }

    List<BudgetScenarioVariable> variableList =
        budgetScenarioVariableRepository
            .all()
            .filter(
                "self.entryMethod = ?1 AND self.formula IS NOT NULL",
                BudgetScenarioVariableRepository
                    .BUDGET_SCENARIO_VARIABLE_ENTRY_METHOD_TYPE_SELECT_FORMULA)
            .order("id")
            .fetch();
    if (ObjectUtils.isEmpty(variableList)) {
      return;
    }
    for (BudgetScenarioVariable budgetScenarioVariable : variableList) {
      BudgetScenarioLine budgetScenarioLine = new BudgetScenarioLine();
      budgetScenarioLine.setBudgetScenarioVariable(budgetScenarioVariable);
      budgetScenario.addBudgetScenarioLineListItem(budgetScenarioLine);
    }

    for (int yearNumber = 1;
        Math.min(budgetScenario.getYearSet().size(), 6) >= yearNumber;
        yearNumber++) {
      Map<String, Object> variableMap = buildVariableMap(budgetScenario, yearNumber);
      if (!ObjectUtils.isEmpty(variableMap)) {
        Iterator<Map.Entry<String, Object>> iterator = variableMap.entrySet().iterator();
        while (iterator.hasNext()) {
          Map.Entry entry = iterator.next();
          BudgetScenarioLine budgetScenarioLine =
              budgetScenario.getBudgetScenarioLineList().stream()
                  .filter(line -> entry.getKey().equals(line.getBudgetScenarioVariable().getCode()))
                  .findFirst()
                  .orElse(null);
          if (budgetScenarioLine != null) {
            setYearValue(
                budgetScenarioLine,
                yearNumber,
                currencyScaleServiceBudget.getCompanyScaledValue(
                    budgetScenario, (BigDecimal) entry.getValue()));
          }
        }
      }
    }
  }

  protected void setYearValue(
      BudgetScenarioLine budgetScenarioLine, int yearNumber, BigDecimal amount) {
    switch (yearNumber) {
      case 2:
        budgetScenarioLine.setYear2Value(amount);
        break;
      case 3:
        budgetScenarioLine.setYear3Value(amount);
        break;
      case 4:
        budgetScenarioLine.setYear4Value(amount);
        break;
      case 5:
        budgetScenarioLine.setYear5Value(amount);
        break;
      case 6:
        budgetScenarioLine.setYear6Value(amount);
        break;
      default:
        budgetScenarioLine.setYear1Value(amount);
    }
  }

  @Override
  @Transactional
  public void draftScenario(BudgetScenario budgetScenario) {
    List<BudgetScenarioLine> budgetScenarioLineList =
        budgetScenario.getBudgetScenarioLineList().stream()
            .filter(
                line ->
                    line.getBudgetScenarioVariable().getEntryMethod()
                        == BudgetScenarioVariableRepository
                            .BUDGET_SCENARIO_VARIABLE_ENTRY_METHOD_TYPE_SELECT_FORMULA)
            .collect(Collectors.toList());

    for (BudgetScenarioLine budgetScenarioLine : budgetScenarioLineList) {
      budgetScenario.removeBudgetScenarioLineListItem(budgetScenarioLine);
    }

    budgetScenario.setStatusSelect(BudgetScenarioRepository.BUDGET_SCENARIO_STATUS_SELECT_DRAFT);
  }
}
