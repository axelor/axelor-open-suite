package com.axelor.apps.mobilesettings.rest.dto;

public class MobileChartValueResponse {
  protected String label;
  protected String value;

  public MobileChartValueResponse(String label, String value) {
    this.label = label;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public String getValue() {
    return value;
  }
}
