package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;

public class AnalyticAxisServiceImpl implements AnalyticAxisService {

  protected AnalyticAxisFetchService analyticAxisFetchService;

  @Inject
  public AnalyticAxisServiceImpl(AnalyticAxisFetchService analyticAxisFetchService) {
    this.analyticAxisFetchService = analyticAxisFetchService;
  }

  @Override
  public boolean checkCompanyOnMoveLine(AnalyticAxis analyticAxis) {
    if (analyticAxis != null && analyticAxis.getCompany() != null) {
      return !ObjectUtils.isEmpty(
          analyticAxisFetchService.findByAnalyticAxisAndAnotherCompany(
              analyticAxis, analyticAxis.getCompany()));
    }
    return false;
  }

  @Override
  public Long getAnalyticGroupingId(AnalyticAxis analyticAxis, Integer position) {
    switch (position) {
      case 1:
        if (analyticAxis.getAnalyticGrouping1() != null) {
          return analyticAxis.getAnalyticGrouping1().getId();
        }
        break;
      case 2:
        if (analyticAxis.getAnalyticGrouping2() != null) {
          return analyticAxis.getAnalyticGrouping2().getId();
        }
        break;
      case 3:
        if (analyticAxis.getAnalyticGrouping3() != null) {
          return analyticAxis.getAnalyticGrouping3().getId();
        }
        break;
      case 4:
        if (analyticAxis.getAnalyticGrouping4() != null) {
          return analyticAxis.getAnalyticGrouping4().getId();
        }
        break;
      case 5:
        if (analyticAxis.getAnalyticGrouping5() != null) {
          return analyticAxis.getAnalyticGrouping5().getId();
        }
        break;
      case 6:
        if (analyticAxis.getAnalyticGrouping6() != null) {
          return analyticAxis.getAnalyticGrouping6().getId();
        }
        break;
      case 7:
        if (analyticAxis.getAnalyticGrouping7() != null) {
          return analyticAxis.getAnalyticGrouping7().getId();
        }
        break;
      case 8:
        if (analyticAxis.getAnalyticGrouping8() != null) {
          return analyticAxis.getAnalyticGrouping8().getId();
        }
        break;
      case 9:
        if (analyticAxis.getAnalyticGrouping9() != null) {
          return analyticAxis.getAnalyticGrouping9().getId();
        }
        break;
      case 10:
        if (analyticAxis.getAnalyticGrouping10() != null) {
          return analyticAxis.getAnalyticGrouping10().getId();
        }
        break;
    }
    return (long) 0;
  }
}
