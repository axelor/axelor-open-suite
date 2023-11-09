package com.axelor.apps.mobilesettings.rest.dto;

import java.util.List;

public class MobileDashboardLineResponse {

  protected String name;
  protected List<MobileChartNoVersionResponse> chartList;

  public MobileDashboardLineResponse(String name, List<MobileChartNoVersionResponse> chartList) {
    this.name = name;
    this.chartList = chartList;
  }

  public String getName() {
    return name;
  }

  public List<MobileChartNoVersionResponse> getChartList() {
    return chartList;
  }
}
