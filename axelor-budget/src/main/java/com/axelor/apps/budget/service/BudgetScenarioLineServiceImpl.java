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

import com.axelor.apps.base.db.Year;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.repo.BudgetScenarioLineRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
  public List<Map<String, Object>> getLineUsingSection(
      BudgetLevel section,
      Set<BudgetScenarioVariable> budgetScenarioVariableSet,
      List<BudgetScenarioLine> budgetScenarioLineOriginList,
      List<Map<String, Object>> budgetScenarioLineList) {
    if (!ObjectUtils.isEmpty(budgetScenarioVariableSet)) {
      for (BudgetScenarioVariable budgetScenarioVariable : budgetScenarioVariableSet) {
        BudgetScenarioLine budgetScenarioLine =
            budgetScenarioLineOriginList.stream()
                .filter(line -> budgetScenarioVariable.equals(line.getBudgetScenarioVariable()))
                .findFirst()
                .orElse(null);
        if (budgetScenarioLine != null) {
          BudgetScenarioLine optLine = budgetScenarioLineRepository.copy(budgetScenarioLine, false);

          Map<String, Object> optLineContext = Mapper.toMap(optLine);
          optLineContext.put("budgetLevel", section.getCode());
          budgetScenarioLineList.add(optLineContext);
        }
      }
    } else if (section != null && !ObjectUtils.isEmpty(section.getBudgetLevelList())) {
      for (BudgetLevel child : section.getBudgetLevelList()) {
        budgetScenarioLineList =
            getLineUsingSection(
                child,
                child.getBudgetScenarioVariableSet(),
                budgetScenarioLineOriginList,
                budgetScenarioLineList);
      }
    }
    return budgetScenarioLineList;
  }
}
