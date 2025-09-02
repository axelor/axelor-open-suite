package com.axelor.apps.businessproject.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppBudget;

public class AccountConfigController {

  public void manageBudgetKey(ActionRequest request, ActionResponse response) {
    AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
    AppBudget appBudget = Beans.get(AppBudgetService.class).getAppBudget();
    if (appBudget == null) {
      return;
    }

    response.setAttr("enableBudgetKey", "readonly", appBudget.getEnableProject());
    response.setAttr("$budgetKeyDisable", "hidden", !appBudget.getEnableProject());
  }
}
