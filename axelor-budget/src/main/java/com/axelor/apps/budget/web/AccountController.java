package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.service.BudgetAccountService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AccountController {

  public void hideBudgetPanel(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);

      boolean isShow = Beans.get(BudgetAccountService.class).checkAccountType(account);

      response.setAttr("relatedBudgetPanel", "hidden", !isShow);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
