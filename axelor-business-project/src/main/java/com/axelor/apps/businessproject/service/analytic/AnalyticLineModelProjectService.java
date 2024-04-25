package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.model.AnalyticLineProjectModel;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public interface AnalyticLineModelProjectService {

  AnalyticLineProjectModel getAndComputeAnalyticDistribution(
      AnalyticLineProjectModel analyticLineProjectModel) throws AxelorException;

  boolean analyticDistributionTemplateRequired(AnalyticLineModel analyticLineModel)
      throws AxelorException;
}
