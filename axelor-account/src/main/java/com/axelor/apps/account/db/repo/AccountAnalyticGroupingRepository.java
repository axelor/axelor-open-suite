package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AnalyticGrouping;
import com.axelor.exception.AxelorException;
import com.axelor.meta.CallMethod;

public class AccountAnalyticGroupingRepository extends AnalyticGroupingRepository {

  @CallMethod
  public AnalyticGrouping calculateFullName(AnalyticGrouping analyticGrouping)
      throws AxelorException {
    if (analyticGrouping.getCode() != null && analyticGrouping.getName() != null) {
      analyticGrouping.setFullName(analyticGrouping.getCode() + "_" + analyticGrouping.getName());
    }
    return analyticGrouping;
  }
}
