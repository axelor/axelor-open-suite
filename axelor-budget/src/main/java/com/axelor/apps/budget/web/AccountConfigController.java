package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.service.BudgetAccountConfigService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AccountConfigController {

  public void checkBudgetKey(ActionRequest request, ActionResponse response) {
    try {
      AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
      Beans.get(BudgetAccountConfigService.class).checkBudgetKey(accountConfig);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
