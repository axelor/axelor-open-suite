package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.repo.BudgetScenarioRepository;
import com.axelor.apps.budget.db.repo.BudgetScenarioVariableRepository;
import com.axelor.common.ObjectUtils;
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

  @Inject
  public BudgetScenarioServiceImpl(
      BudgetScenarioVariableRepository budgetScenarioVariableRepository,
      BudgetScenarioRepository budgetScenarioRepository) {
    this.budgetScenarioVariableRepository = budgetScenarioVariableRepository;
    this.budgetScenarioRepository = budgetScenarioRepository;
  }

  @Override
  public Map<String, Object> buildVariableMap(BudgetScenario budgetScenario, int yearNumber)
      throws AxelorException {
    Map<String, Object> variableAmountMap = new HashMap<>();
    if (budgetScenario != null
        && !ObjectUtils.isEmpty(budgetScenario.getBudgetScenarioLineList())) {
      for (BudgetScenarioLine budgetScenarioLine : budgetScenario.getBudgetScenarioLineList()) {
        BigDecimal yearValue = getYearValue(budgetScenarioLine, yearNumber);
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
    computeFormulaVariable(variableAmountMap);

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
      BigDecimal yearValue = getYearValue(budgetScenarioLine, yearNumber);
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

  protected Map<String, Object> computeFormulaVariable(Map<String, Object> variableAmountMap)
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
      fillVariableAmountMapWithFormula(variableAmountMap, variableList);
      replaceNullValuesInVariableMap(variableAmountMap);
    }
    return variableAmountMap;
  }

  protected void fillVariableAmountMapWithFormula(
      Map<String, Object> variableAmountMap, List<BudgetScenarioVariable> variableList)
      throws AxelorException {
    for (BudgetScenarioVariable variable : variableList) {
      variableAmountMap.put(variable.getCode(), null);
    }
    int nullCount = -1;
    int previousNullCount;

    while (nullCount != 0) {
      previousNullCount = nullCount;

      nullCount = this.fillAllMap(variableAmountMap);

      if (nullCount == previousNullCount) {
        return;
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
          // throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
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
  @Transactional
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
            setYearValue(budgetScenarioLine, yearNumber, (BigDecimal) entry.getValue());
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
