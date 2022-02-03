package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.service.analytic.AnalyticAxisByCompanyService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.HandleExceptionResponse;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticAxisByCompanyController {

  @HandleExceptionResponse
  public void setAxisDomain(ActionRequest request, ActionResponse response) throws AxelorException {
    AccountConfig accountConfig = request.getContext().getParent().asType(AccountConfig.class);
    if (accountConfig != null) {
      String domain = Beans.get(AnalyticAxisByCompanyService.class).getAxisDomain(accountConfig);
      if (domain != null) {
        response.setAttr("analyticAxis", "domain", domain);
      }
    }
  }

  @HandleExceptionResponse
  public void setOrderSelect(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountConfig accountConfig = request.getContext().getParent().asType(AccountConfig.class);
    if (accountConfig != null) {
      Integer axisListSize = accountConfig.getAnalyticAxisByCompanyList().size();

      if (axisListSize < accountConfig.getNbrOfAnalyticAxisSelect()) {
        response.setValue("orderSelect", axisListSize + 1);
      }
    }
  }
}
