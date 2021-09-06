package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.service.AnalyticAxisByCompanyService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticAxisByCompanyController {

  public void setAxisDomain(ActionRequest request, ActionResponse response) {
    try {
      AnalyticAxisByCompany analyticAxisByCompany =
          request.getContext().asType(AnalyticAxisByCompany.class);

      if (analyticAxisByCompany.getAccountConfig() != null) {
        String domain =
            Beans.get(AnalyticAxisByCompanyService.class)
                .getAxisDomain(analyticAxisByCompany.getAccountConfig());
        if (domain != null) {
          response.setAttr("analyticAxis", "domain", domain);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setOrderSelect(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AccountConfig accountConfig = request.getContext().getParent().asType(AccountConfig.class);
      if (accountConfig != null) {
        Integer axisListSize = accountConfig.getAnalyticAxisByCompanyList().size();

        if (axisListSize < accountConfig.getNbrOfAnalyticAxisSelect()) {
          response.setValue("orderSelect", axisListSize + 1);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
