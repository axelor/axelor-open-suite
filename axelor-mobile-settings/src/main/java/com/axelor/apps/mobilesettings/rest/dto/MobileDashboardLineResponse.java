package com.axelor.apps.mobilesettings.rest.dto;

import java.util.List;

public class MobileDashboardLineResponse {

  protected String name;
  protected List<MobileChartResponse> chartList;

  public MobileDashboardLineResponse(String name, List<MobileChartResponse> chartList) {
    this.name = name;
    this.chartList = chartList;
  }

  public String getName() {
    return name;
  }

  public List<MobileChartResponse> getChartList() {
    return chartList;
  }
}
