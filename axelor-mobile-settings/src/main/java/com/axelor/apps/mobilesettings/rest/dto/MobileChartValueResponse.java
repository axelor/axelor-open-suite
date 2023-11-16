package com.axelor.apps.mobilesettings.rest.dto;

public class MobileChartValueResponse {
  protected String label;
  protected long value;

  public MobileChartValueResponse(String label, long value) {
    this.label = label;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public long getValue() {
    return value;
  }
}
