package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class MobileChartResponse extends ResponseStructure {
  protected Long chartId;
  protected String chartName;
  protected String chartType;
  protected List<MobileChartValueResponse> valueList;

  public MobileChartResponse(
      MobileChart mobileChart, String chartName, List<MobileChartValueResponse> valueList) {
    super(mobileChart.getVersion());
    this.chartId = mobileChart.getId();
    this.chartName = chartName;
    this.chartType = mobileChart.getChartTypeSelect();
    this.valueList = valueList;
  }

  public Long getChartId() {
    return chartId;
  }

  public String getChartName() {
    return chartName;
  }

  public String getChartType() {
    return chartType;
  }

  public List<MobileChartValueResponse> getValueList() {
    return valueList;
  }
}
