package com.axelor.apps.budget.service;

import com.axelor.apps.base.db.Year;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.repo.BudgetScenarioLineRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BudgetScenarioLineServiceImpl implements BudgetScenarioLineService {
  protected BudgetScenarioLineRepository budgetScenarioLineRepository;

  @Inject
  public BudgetScenarioLineServiceImpl(BudgetScenarioLineRepository budgetScenarioLineRepository) {
    this.budgetScenarioLineRepository = budgetScenarioLineRepository;
  }

  @Override
  public ArrayList<Integer> getFiscalYears(BudgetScenario budgetScenario) {
    ArrayList<Integer> yearIntegers = new ArrayList<>();
    Set<Year> yearSet = budgetScenario.getYearSet();
    for (Year year : yearSet) {
      int yearValue = year.getFromDate().getYear();

      yearIntegers.add(yearValue);
    }
    Collections.sort(yearIntegers);
    return yearIntegers;
  }

  @Override
  public void removeUnusedYears(BudgetScenarioLine budgetScenarioLine, int size) {
    if (size <= 5) {
      budgetScenarioLine.setYear6Value(BigDecimal.ZERO);
    }
    if (size <= 4) {
      budgetScenarioLine.setYear5Value(BigDecimal.ZERO);
    }
    if (size <= 3) {
      budgetScenarioLine.setYear4Value(BigDecimal.ZERO);
    }
    if (size <= 2) {
      budgetScenarioLine.setYear3Value(BigDecimal.ZERO);
    }
    if (size <= 1) {
      budgetScenarioLine.setYear2Value(BigDecimal.ZERO);
    }
    if (size <= 0) {
      budgetScenarioLine.setYear1Value(BigDecimal.ZERO);
    }
  }

  @Override
  public List<BudgetScenarioLine> getLineUsingSection(
      BudgetLevel section,
      List<BudgetScenarioLine> budgetScenarioLineOriginList,
      List<BudgetScenarioLine> budgetScenarioLineList) {
    if (!ObjectUtils.isEmpty(section.getBudgetScenarioVariableSet())) {
      for (BudgetScenarioVariable budgetScenarioVariable : section.getBudgetScenarioVariableSet()) {
        BudgetScenarioLine budgetScenarioLine =
            budgetScenarioLineOriginList.stream()
                .filter(line -> budgetScenarioVariable.equals(line.getBudgetScenarioVariable()))
                .findFirst()
                .orElse(null);
        if (budgetScenarioLine != null) {
          BudgetScenarioLine optLine = budgetScenarioLineRepository.copy(budgetScenarioLine, false);
          optLine.setBudgetLevel(section);
          optLine.setParentBudgetLevel(section.getParentBudgetLevel());
          budgetScenarioLineList.add(optLine);
        }
      }
    }
    return budgetScenarioLineList;
  }
}
