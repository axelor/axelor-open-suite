package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

public class BudgetGroupServiceImpl implements BudgetGroupService {

  protected GlobalBudgetRepository globalBudgetRepo;

  @Inject
  public BudgetGroupServiceImpl(GlobalBudgetRepository globalBudgetRepo) {
    this.globalBudgetRepo = globalBudgetRepo;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(
      Budget budget, BudgetLevel parent, GlobalBudget global, String typeSelect) {
    Map<String, Object> valuesMap = new HashMap<>();

    fillParentFields(budget, parent, global);

    valuesMap.put("sourceSelect", BudgetLevelRepository.BUDGET_LEVEL_SOURCE_CUSTOM);
    valuesMap.put("fromDate", budget.getFromDate());
    valuesMap.put("toDate", budget.getToDate());
    valuesMap.put("company", budget.getCompany());
    valuesMap.put("inChargeUser", budget.getInChargeUser());
    valuesMap.put("statusSelect", BudgetRepository.STATUS_DRAFT);
    valuesMap.put("typeSelect", typeSelect);
    valuesMap.put("budgetLevel", budget.getBudgetLevel());
    valuesMap.put("globalBudget", budget.getGlobalBudget());

    return valuesMap;
  }

  protected void fillParentFields(Budget budget, BudgetLevel parent, GlobalBudget global) {
    if (parent != null) {
      budget.setBudgetLevel(parent);
      budget.setInChargeUser(parent.getBudgetManager());
      budget.setCompany(parent.getCompany());
      budget.setFromDate(parent.getFromDate());
      budget.setToDate(parent.getToDate());
    } else if (global != null) {
      budget.setGlobalBudget(global);
      budget.setInChargeUser(global.getBudgetManager());
      budget.setCompany(global.getCompany());
      budget.setFromDate(global.getFromDate());
      budget.setToDate(global.getToDate());
    }

    if (budget.getCompany() == null) {
      budget.setCompany(Optional.of(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
    }
  }
}
