package com.axelor.apps.budget.web;

import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.service.BudgetScenarioLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.*;

public class BudgetScenarioLineController {
  public void changeColumnsNames(ActionRequest request, ActionResponse response) {
    BudgetScenario budgetScenario = request.getContext().getParent().asType(BudgetScenario.class);
    ArrayList<Integer> yearsList =
        Beans.get(BudgetScenarioLineService.class).getFiscalYears(budgetScenario);

    for (int i = 0; i < Math.min(yearsList.size(), 6); i++) {
      if (yearsList.contains(yearsList.get(i))) {
        String fieldName = "year" + (i + 1) + "Value";
        response.setAttr(fieldName, "hidden", false);
        response.setAttr(fieldName, "title", Integer.toString(yearsList.get(i)));
      }
    }
  }
}
