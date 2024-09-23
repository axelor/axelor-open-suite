package com.axelor.apps.businessproject.rest.dto;

import java.math.BigDecimal;

public class ProjectReportingIndicatorResponse {

  protected String title;
  protected BigDecimal value;
  protected String unit;

  public ProjectReportingIndicatorResponse(String title, BigDecimal value, String unit) {
    this.title = title;
    this.value = value;
    this.unit = unit;
  }

  public String getTitle() {
    return title;
  }

  public BigDecimal getValue() {
    return value;
  }

  public String getUnit() {
    return unit;
  }
}
