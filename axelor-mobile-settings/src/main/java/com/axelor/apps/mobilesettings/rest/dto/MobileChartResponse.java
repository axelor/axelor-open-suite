package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class MobileChartResponse extends ResponseStructure {
  protected String chartName;
  protected List<MobileChartValueResponse> valueList;

  public MobileChartResponse(
      MobileChart mobileChart, String chartName, List<MobileChartValueResponse> valueList) {
    super(mobileChart.getVersion());
    this.chartName = chartName;
    this.valueList = valueList;
  }

  public String getChartName() {
    return chartName;
  }

  public List<MobileChartValueResponse> getValueList() {
    return valueList;
  }
}
