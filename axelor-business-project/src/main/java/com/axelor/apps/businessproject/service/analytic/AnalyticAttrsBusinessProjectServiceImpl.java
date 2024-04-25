package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.analytic.AnalyticAttrsSupplychainServiceImpl;
import com.google.inject.Inject;
import java.util.Map;

public class AnalyticAttrsBusinessProjectServiceImpl extends AnalyticAttrsSupplychainServiceImpl {

  protected AnalyticLineModelProjectService analyticLineModelProjectService;
  protected AnalyticToolService analyticToolService;

  @Inject
  public AnalyticAttrsBusinessProjectServiceImpl(
      AnalyticLineModelService analyticLineModelService,
      AnalyticLineModelProjectService analyticLineModelProjectService,
      AnalyticToolService analyticToolService) {
    super(analyticLineModelService);
    this.analyticLineModelProjectService = analyticLineModelProjectService;
    this.analyticToolService = analyticToolService;
  }

  @Override
  public void addAnalyticDistributionPanelHiddenAttrs(
      AnalyticLineModel analyticLineModel, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    boolean displayPanel = !analyticToolService.isManageAnalytic(analyticLineModel.getCompany());

    this.addAttr("analyticDistributionPanel", "hidden", displayPanel, attrsMap);
  }
}
