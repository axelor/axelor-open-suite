package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;

public interface AnalyticAxisService {

  public boolean checkCompanyOnMoveLine(AnalyticAxis analyticAxis);

  public Long getAnalyticGroupingId(AnalyticAxis analyticAxis, Integer position);
}
