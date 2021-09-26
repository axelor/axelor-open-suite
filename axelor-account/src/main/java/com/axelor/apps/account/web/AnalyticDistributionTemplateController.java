package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.service.AnalyticDistributionTemplateService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticDistributionTemplateController {

  public void validateTemplatePercentages(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      if (!Beans.get(AnalyticDistributionTemplateService.class)
          .validateTemplatePercentages(analyticDistributionTemplate)) {
        response.setError(
            "The distribution is wrong, some axes percentage values are not equal to 100%");
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void personalize(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      if (analyticDistributionTemplate != null && !analyticDistributionTemplate.getIsSpecific()) {
        AnalyticDistributionTemplate specificAnalyticDistributionTemplate =
            Beans.get(AnalyticDistributionTemplateService.class)
                .personalizeAnalyticDistributionTemplate(analyticDistributionTemplate);
        response.setView(
            ActionView.define("Specific Analytic Distribution Template")
                .model(AnalyticDistributionTemplate.class.getName())
                .add("form", "analytic-distribution-template-form")
                .param("popup", "true")
                .param("forceEdit", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "true")
                .context("_showRecord", specificAnalyticDistributionTemplate.getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
