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
package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GlobalBudgetToolsServiceImpl implements GlobalBudgetToolsService {

  @Inject
  public GlobalBudgetToolsServiceImpl() {}

  @Override
  public List<Budget> getAllBudgets(GlobalBudget globalBudget) {
    List<Budget> budgetList = new ArrayList<>();
    if (globalBudget == null) {
      return budgetList;
    }

    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetList = getAllBudgets(budgetLevel, budgetList);
      }
    } else if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      budgetList.addAll(globalBudget.getBudgetList());
    }

    return budgetList;
  }

  @Override
  public List<Budget> getAllBudgets(BudgetLevel budgetLevel, List<Budget> budgetList) {
    if (budgetLevel == null) {
      return budgetList;
    }

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetList.addAll(budgetLevel.getBudgetList());
      return budgetList;
    }
    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      for (BudgetLevel child : budgetLevel.getBudgetLevelList()) {
        budgetList = getAllBudgets(child, budgetList);
      }
    }
    return budgetList;
  }

  @Override
  public List<Long> getAllBudgetIds(GlobalBudget globalBudget) {
    List<Budget> budgetList = getAllBudgets(globalBudget);
    List<Long> budgetIdList = new ArrayList<>();

    if (!ObjectUtils.isEmpty(budgetList)) {
      budgetIdList = budgetList.stream().map(Budget::getId).collect(Collectors.toList());
    } else {
      budgetIdList.add(0L);
    }

    return budgetIdList;
  }

  @Override
  public List<Long> getAllBudgetLineIds(GlobalBudget globalBudget) {
    List<Long> budgetLineIdList = new ArrayList<>();
    List<Budget> budgetList = getAllBudgets(globalBudget);

    if (!ObjectUtils.isEmpty(budgetList)) {
      budgetLineIdList =
          budgetList.stream()
              .map(Budget::getBudgetLineList)
              .flatMap(Collection::stream)
              .map(BudgetLine::getId)
              .collect(Collectors.toList());
    }
    if (ObjectUtils.isEmpty(budgetLineIdList)) {
      budgetLineIdList.add(0L);
    }

    return budgetLineIdList;
  }

  @Override
  public List<BudgetLevel> getAllBudgetLevels(GlobalBudget globalBudget) {
    List<BudgetLevel> budgetLevelList = new ArrayList<>();
    if (globalBudget == null) {
      return budgetLevelList;
    }

    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      budgetLevelList.addAll(globalBudget.getBudgetLevelList());

      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelList = getAllBudgetLevels(budgetLevel, budgetLevelList);
      }
    }

    return budgetLevelList;
  }

  @Override
  public List<BudgetLevel> getAllBudgetLevels(
      BudgetLevel budgetLevel, List<BudgetLevel> budgetLevelList) {
    if (budgetLevel == null) {
      return budgetLevelList;
    }

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevelList.addAll(budgetLevel.getBudgetLevelList());
      for (BudgetLevel child : budgetLevel.getBudgetLevelList()) {
        getAllBudgetLevels(child, budgetLevelList);
      }
    }

    return budgetLevelList;
  }

  @Override
  public List<Long> getAllBudgetLevelIds(GlobalBudget globalBudget) {
    List<Long> budgetLevelIdList = new ArrayList<>();
    List<BudgetLevel> budgetLevelList = getAllBudgetLevels(globalBudget);
    if (!ObjectUtils.isEmpty(budgetLevelList)) {
      budgetLevelIdList =
          budgetLevelList.stream().map(BudgetLevel::getId).collect(Collectors.toList());
    } else {
      budgetLevelIdList.add(0L);
    }

    return budgetLevelIdList;
  }

  @Override
  public void fillGlobalBudgetOnBudget(GlobalBudget globalBudget) {
    if (globalBudget == null || ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      return;
    }

    List<Budget> budgetList = getAllBudgets(globalBudget);

    if (!ObjectUtils.isEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        budget.setGlobalBudget(globalBudget);
      }
    }
  }

  @Override
  public Map<String, Map<String, Object>> manageHiddenAmounts(boolean hidden) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    this.addAttr("budgetLevelList.totalAmountExpected", "hidden", hidden, attrsMap);
    this.addAttr("budgetLevelList.totalAmountAvailable", "hidden", hidden, attrsMap);
    this.addAttr("budgetLevelList.totalAmountCommitted", "hidden", hidden, attrsMap);
    this.addAttr("budgetLevelList.realizedWithNoPo", "hidden", hidden, attrsMap);
    this.addAttr("budgetLevelList.realizedWithPo", "hidden", hidden, attrsMap);
    this.addAttr("budgetLevelList.totalFirmGap", "hidden", hidden, attrsMap);
    this.addAttr("budgetList.totalAmountExpected", "hidden", hidden, attrsMap);
    this.addAttr("budgetList.totalAmountCommitted", "hidden", hidden, attrsMap);
    this.addAttr("budgetList.totalAmountRealized", "hidden", hidden, attrsMap);
    this.addAttr("budgetList.availableAmount", "hidden", hidden, attrsMap);

    return attrsMap;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }
}
