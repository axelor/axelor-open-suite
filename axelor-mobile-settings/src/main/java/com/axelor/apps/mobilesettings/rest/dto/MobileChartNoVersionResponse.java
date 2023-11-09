package com.axelor.apps.mobilesettings.rest.dto;

import java.util.List;

public class MobileChartNoVersionResponse {
  protected String chartName;
  protected List<MobileChartValueResponse> valueList;

  public MobileChartNoVersionResponse(String chartName, List<MobileChartValueResponse> valueList) {

    this.chartName = chartName;
    this.valueList = valueList;
  }

  public String getChartName() {
    return chartName;
  }

  public List<MobileChartValueResponse> getMobileChartValueResponseList() {
    return valueList;
  }
}
