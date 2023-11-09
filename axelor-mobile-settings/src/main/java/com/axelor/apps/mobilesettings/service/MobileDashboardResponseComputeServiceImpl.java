package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.db.MobileDashboardLine;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartNoVersionResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileDashboardLineResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileDashboardResponse;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class MobileDashboardResponseComputeServiceImpl
    implements MobileDashboardResponseComputeService {
  protected MobileChartResponseComputeService mobileChartResponseComputeService;

  @Inject
  public MobileDashboardResponseComputeServiceImpl(
      MobileChartResponseComputeService mobileChartResponseComputeService) {
    this.mobileChartResponseComputeService = mobileChartResponseComputeService;
  }

  @Override
  public MobileDashboardResponse computeMobileDashboardResponse(MobileDashboard mobileDashboard) {
    List<MobileDashboardLineResponse> mobileDashboardLineResponseList = new ArrayList<>();
    for (MobileDashboardLine mobileDashboardLine : mobileDashboard.getMobileDashboardLineList()) {
      List<MobileChartNoVersionResponse> mobileChartResponseList = new ArrayList<>();

      addMobileChartResponses(mobileDashboardLine, mobileChartResponseList);

      mobileDashboardLineResponseList.add(
          new MobileDashboardLineResponse(mobileDashboardLine.getName(), mobileChartResponseList));
    }
    return new MobileDashboardResponse(
        mobileDashboard, mobileDashboard.getName(), mobileDashboardLineResponseList);
  }

  protected void addMobileChartResponses(
      MobileDashboardLine mobileDashboardLine,
      List<MobileChartNoVersionResponse> mobileChartResponseList) {
    addMobileChartResponse(mobileDashboardLine.getMobileChart1(), mobileChartResponseList);
    addMobileChartResponse(mobileDashboardLine.getMobileChart2(), mobileChartResponseList);
    addMobileChartResponse(mobileDashboardLine.getMobileChart3(), mobileChartResponseList);
    addMobileChartResponse(mobileDashboardLine.getMobileChart4(), mobileChartResponseList);
  }

  protected void addMobileChartResponse(
      MobileChart mobileChart, List<MobileChartNoVersionResponse> mobileChartResponseList) {
    if (mobileChart != null) {
      mobileChartResponseList.add(
          mobileChartResponseComputeService.computeMobileChartNoVersionResponse(mobileChart));
    }
  }
}
