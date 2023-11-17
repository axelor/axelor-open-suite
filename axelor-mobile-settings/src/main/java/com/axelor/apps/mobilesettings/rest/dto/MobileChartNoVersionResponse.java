package com.axelor.apps.mobilesettings.rest.dto;

import java.util.List;

public class MobileChartNoVersionResponse {
  protected String chartName;
  protected String chartType;
  protected List<MobileChartValueResponse> valueList;

  public MobileChartNoVersionResponse(
      String chartName, String chartType, List<MobileChartValueResponse> valueList) {

    this.chartName = chartName;
    this.chartType = chartType;
    this.valueList = valueList;
  }

  public String getChartName() {
    return chartName;
  }

  public String getChartType() {
    return chartType;
  }

  public List<MobileChartValueResponse> getMobileChartValueResponseList() {
    return valueList;
  }
}
