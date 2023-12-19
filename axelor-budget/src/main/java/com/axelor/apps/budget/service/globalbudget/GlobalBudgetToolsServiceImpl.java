package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
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
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      budgetList = globalBudget.getBudgetList();
    }
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetList = getAllBudgets(budgetLevel, budgetList);
      }
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
    List<Long> budgetIdList =
        getAllBudgets(globalBudget).stream().map(Budget::getId).collect(Collectors.toList());
    if (ObjectUtils.isEmpty(budgetIdList)) {
      budgetIdList.add(0L);
    }

    return budgetIdList;
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
