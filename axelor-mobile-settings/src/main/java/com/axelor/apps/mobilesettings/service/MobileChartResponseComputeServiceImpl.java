package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartResponse;
import com.google.inject.Inject;

public class MobileChartResponseComputeServiceImpl implements MobileChartResponseComputeService {
  protected MobileChartService mobileChartService;

  @Inject
  public MobileChartResponseComputeServiceImpl(MobileChartService mobileChartService) {
    this.mobileChartService = mobileChartService;
  }

  @Override
  public MobileChartResponse computeMobileChartResponse(MobileChart mobileChart)
      throws AxelorException {
    return new MobileChartResponse(
        mobileChart, mobileChart.getName(), mobileChartService.getValueList(mobileChart));
  }
}
