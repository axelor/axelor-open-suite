package com.axelor.apps.hr.rest.dto;

import java.math.BigDecimal;

public class LeaveDaysToDateResponse {
  private BigDecimal leaveDays;

  public LeaveDaysToDateResponse(BigDecimal leaveDays) {
    this.leaveDays = leaveDays;
  }

  public BigDecimal getLeaveDays() {
    return leaveDays;
  }
}
