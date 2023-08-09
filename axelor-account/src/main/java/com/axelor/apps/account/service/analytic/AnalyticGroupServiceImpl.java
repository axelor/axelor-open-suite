package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AnalyticGroupServiceImpl implements AnalyticGroupService {

  protected AnalyticAttrsService analyticAttrsService;
  protected AnalyticLineService analyticLineService;

  @Inject
  public AnalyticGroupServiceImpl(
      AnalyticAttrsService analyticAttrsService, AnalyticLineService analyticLineService) {
    this.analyticAttrsService = analyticAttrsService;
    this.analyticLineService = analyticLineService;
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticAxisDomainAttrsMap(
      AnalyticLine analyticLineModel, Company company) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    analyticAttrsService.addAxisDomains(analyticLineModel, company, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getAnalyticAccountValueMap(AnalyticLine analyticLine, Company company)
      throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    analyticLineService.setAnalyticAccount(analyticLine, company);

    valuesMap.put("axis1AnalyticAccount", analyticLine.getAxis1AnalyticAccount());
    valuesMap.put("axis2AnalyticAccount", analyticLine.getAxis2AnalyticAccount());
    valuesMap.put("axis3AnalyticAccount", analyticLine.getAxis3AnalyticAccount());
    valuesMap.put("axis4AnalyticAccount", analyticLine.getAxis4AnalyticAccount());
    valuesMap.put("axis5AnalyticAccount", analyticLine.getAxis5AnalyticAccount());

    return valuesMap;
  }
}
