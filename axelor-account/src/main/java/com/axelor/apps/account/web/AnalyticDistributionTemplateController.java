package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.service.AnalyticDistributionTemplateService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticDistributionTemplateController {

  public void validateTemplatePercentages(ActionRequest request, ActionResponse response) {
    AnalyticDistributionTemplate analyticDistributionTemplate =
        request.getContext().asType(AnalyticDistributionTemplate.class);
    if (!Beans.get(AnalyticDistributionTemplateService.class)
        .validateTemplatePercentages(analyticDistributionTemplate)) {
      response.setError(
          "The distribution is wrong, some axes percentage values are not equal to 100%");
    }
  }
}
