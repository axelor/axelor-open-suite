package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import java.util.Map;

public interface AnalyticAttrsSupplychainService {

  void addAnalyticDistributionPanelHiddenAttrs(
      AnalyticLineModel analyticLineModel, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;
}
