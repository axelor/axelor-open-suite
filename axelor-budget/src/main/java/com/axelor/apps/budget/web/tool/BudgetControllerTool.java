package com.axelor.apps.budget.web.tool;

import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;

public class BudgetControllerTool {

  public static void verifyMissingBudget(ActionResponse response) {
    Boolean isError = Beans.get(AppBudgetService.class).isMissingBudgetCheckError();
    if (isError != null) {
      if (isError) {
        response.setError(I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND_ERROR));
      } else {
        response.setAlert(I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND));
      }
    }
  }

  public static void verifyBudgetExceed(
      String budgetExceedAlert, boolean isOrder, ActionResponse response) {
    if (!Strings.isNullOrEmpty(budgetExceedAlert)) {
      Boolean isError = Beans.get(AppBudgetService.class).isBudgetExceedValuesError(isOrder);
      if (isError != null) {
        budgetExceedAlert =
            Beans.get(BudgetToolsService.class)
                .getBudgetExceedMessage(budgetExceedAlert, isOrder, isError);
        if (isError) {
          response.setError(I18n.get(budgetExceedAlert));
        } else {
          response.setAlert(I18n.get(budgetExceedAlert));
        }
      }
    }
  }
}
