package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartNoVersionResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartResponse;
import com.google.inject.Inject;

public class MobileChartResponseComputeServiceImpl implements MobileChartResponseComputeService {
  protected MobileChartService mobileChartService;

  @Inject
  public MobileChartResponseComputeServiceImpl(MobileChartService mobileChartService) {
    this.mobileChartService = mobileChartService;
  }

  @Override
  public MobileChartResponse computeMobileChartResponse(MobileChart mobileChart) {
    return new MobileChartResponse(
        mobileChart, mobileChart.getName(), mobileChartService.getValueList(mobileChart));
  }

  @Override
  public MobileChartNoVersionResponse computeMobileChartNoVersionResponse(MobileChart mobileChart) {
    return new MobileChartNoVersionResponse(
        mobileChart.getName(),
        mobileChart.getChartTypeSelect(),
        mobileChartService.getValueList(mobileChart));
  }
}
