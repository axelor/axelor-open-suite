package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
}
