package com.axelor.apps.budget.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.service.GlobalBudgetService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GlobalBudgetController {
  public void checkBudgetDates(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
      if (globalBudget != null) {
        Beans.get(GlobalBudgetService.class).validateDates(globalBudget);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
