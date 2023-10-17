package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AnalyticAttrsSupplychainServiceImpl implements AnalyticAttrsSupplychainService {

  protected AnalyticLineModelService analyticLineModelService;

  @Inject
  public AnalyticAttrsSupplychainServiceImpl(AnalyticLineModelService analyticLineModelService) {
    this.analyticLineModelService = analyticLineModelService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addAnalyticDistributionPanelHiddenAttrs(
      AnalyticLineModel analyticLineModel, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    boolean displayPanel =
        !(analyticLineModelService.productAccountManageAnalytic(analyticLineModel)
            || analyticLineModelService.analyticDistributionTemplateRequired(
                analyticLineModel.getIsPurchase(), analyticLineModel.getCompany()));

    this.addAttr("analyticDistributionPanel", "hidden", displayPanel, attrsMap);
  }
}
