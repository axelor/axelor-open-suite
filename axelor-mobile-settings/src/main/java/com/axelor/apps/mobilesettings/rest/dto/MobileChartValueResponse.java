package com.axelor.apps.mobilesettings.rest.dto;

public class MobileChartValueResponse {
  protected String label;
  protected double value;

  public MobileChartValueResponse(String label, double value) {
    this.label = label;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public double getValue() {
    return value;
  }
}
