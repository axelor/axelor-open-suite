package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticGrouping;
import com.axelor.meta.CallMethod;

public interface AnalyticGroupingService {

  @CallMethod
  AnalyticGrouping calculateFullName(AnalyticGrouping analyticGrouping);
}
