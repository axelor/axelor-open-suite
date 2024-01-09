package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppBudget;

public class AppBudgetController {

  public void manageBudgetKey(ActionRequest request, ActionResponse response) {
    AppBudget appBudget = request.getContext().asType(AppBudget.class);

    boolean isBudgetKeyOnAllCompanies =
        Beans.get(AccountConfigRepository.class)
            .all()
            .fetchStream()
            .noneMatch(AccountConfig::getEnableBudgetKey);

    response.setAttr("enableProject", "readonly", !isBudgetKeyOnAllCompanies);
    response.setAttr("$projectDisable", "hidden", isBudgetKeyOnAllCompanies);
  }
}
