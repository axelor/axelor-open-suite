package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.fixedasset.FixedAssetService;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticDistributionTemplateController {

  public void validateTemplatePercentages(ActionRequest request, ActionResponse response) {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      if (!Beans.get(AnalyticDistributionTemplateService.class)
          .validateTemplatePercentages(analyticDistributionTemplate)) {
        response.setError(
            I18n.get(
                "The configured distribution is incorrect, the sum of percentages for each axis must be equal to 100%"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void calculateAnalyticFixedAsset(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().get("fixedAsset") != null) {
        Long fixedAssetId = (Long) Long.valueOf((Integer) request.getContext().get("fixedAsset"));
        FixedAsset fixedAsset = Beans.get(FixedAssetRepository.class).find(fixedAssetId);
        Beans.get(FixedAssetService.class).updateAnalytic(fixedAsset);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      Beans.get(AnalyticDistributionTemplateService.class)
          .checkAnalyticAccounts(analyticDistributionTemplate);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
