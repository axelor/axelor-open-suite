package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.repo.BudgetScenarioVariableRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BudgetScenarioServiceImpl implements BudgetScenarioService {

  protected BudgetScenarioVariableRepository budgetScenarioVariableRepository;

  @Inject
  public BudgetScenarioServiceImpl(
      BudgetScenarioVariableRepository budgetScenarioVariableRepository) {
    this.budgetScenarioVariableRepository = budgetScenarioVariableRepository;
  }

  @Override
  public Map<String, Object> buildVariableMap(BudgetScenario budgetScenario)
      throws AxelorException {
    Map<String, Object> variableAmountMap = new HashMap<>();
    if (budgetScenario != null
        && !ObjectUtils.isEmpty(budgetScenario.getBudgetScenarioLineList())) {
      for (BudgetScenarioLine budgetScenarioLine : budgetScenario.getBudgetScenarioLineList()) {
        if (!variableAmountMap.containsKey(
            budgetScenarioLine.getBudgetScenarioVariable().getCode())) {
          variableAmountMap.put(
              budgetScenarioLine.getBudgetScenarioVariable().getCode(),
              budgetScenarioLine.getYear1Value());
        } else {
          variableAmountMap.replace(
              budgetScenarioLine.getBudgetScenarioVariable().getCode(),
              ((BigDecimal)
                      variableAmountMap.get(
                          budgetScenarioLine.getBudgetScenarioVariable().getCode()))
                  .add(budgetScenarioLine.getYear1Value()));
        }
      }
    }
    computeFormulaVariable(variableAmountMap);

    return variableAmountMap;
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
}
