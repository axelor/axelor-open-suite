package com.axelor.apps.budget.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.service.advanced.imports.AdvancedImportBudgetService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AdvancedImportBudgetController {

  public void setLevelTypeSelect(ActionRequest request, ActionResponse response) {
    try {
      BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
      Beans.get(AdvancedImportBudgetService.class).setLevelTypeSelect(budgetLevel);
      response.setValues(budgetLevel);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setBudgetLevel(ActionRequest request, ActionResponse response) {

    try {
      Budget budget = request.getContext().asType(Budget.class);
      Beans.get(AdvancedImportBudgetService.class).setBudgetLevel(budget);
      response.setValues(budget);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
