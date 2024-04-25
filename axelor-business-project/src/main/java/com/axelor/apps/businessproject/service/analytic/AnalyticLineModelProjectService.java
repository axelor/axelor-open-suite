package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public interface AnalyticLineModelProjectService {

  boolean analyticDistributionTemplateRequired(AnalyticLineModel analyticLineModel)
      throws AxelorException;
}
