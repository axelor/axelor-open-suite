package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticAccountController {

  public void setParentDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AnalyticAccount analyticAccount = request.getContext().asType(AnalyticAccount.class);

      if (analyticAccount != null
          && analyticAccount.getAnalyticAxis() != null
          && analyticAccount.getAnalyticLevel() != null) {
        Integer level = analyticAccount.getAnalyticLevel().getNbr() + 1;
        response.setAttr(
            "parent",
            "domain",
            "self.analyticLevel.nbr = "
                + level
                + " AND self.analyticAxis.id = "
                + analyticAccount.getAnalyticAxis().getId());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
