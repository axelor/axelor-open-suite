package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.fixedasset.FixedAssetService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
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

  public void calculateAnalyticFixedAsset(ActionRequest request, ActionResponse response)
      throws AxelorException {
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
}
