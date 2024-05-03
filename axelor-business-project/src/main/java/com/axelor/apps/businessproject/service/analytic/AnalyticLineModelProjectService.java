package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.businessproject.model.AnalyticLineProjectModel;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public interface AnalyticLineModelProjectService {

  boolean analyzeAnalyticLineModel(
      AnalyticLineProjectModel analyticLineProjectModel, Company company) throws AxelorException;

  AnalyticLineModel createAnalyticDistributionWithTemplate(
      AnalyticLineProjectModel analyticLineProjectModel) throws AxelorException;

  AnalyticLineProjectModel getAndCopyAnalyticDistribution(
      AnalyticLineProjectModel analyticLineProjectModel) throws AxelorException;

  boolean analyticDistributionTemplateRequired(AnalyticLineModel analyticLineModel)
      throws AxelorException;
}
