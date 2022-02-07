package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticGrouping;

public class AnalyticGroupingServiceImpl implements AnalyticGroupingService {

  public AnalyticGroupingServiceImpl() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public AnalyticGrouping calculateFullName(AnalyticGrouping analyticGrouping) {
    if (analyticGrouping.getCode() != null && analyticGrouping.getName() != null) {
      analyticGrouping.setFullName(analyticGrouping.getCode() + "_" + analyticGrouping.getName());
    }
    return analyticGrouping;
  }
}
