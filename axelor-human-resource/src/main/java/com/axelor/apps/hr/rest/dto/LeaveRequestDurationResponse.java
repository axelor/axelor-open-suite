package com.axelor.apps.hr.rest.dto;

import java.math.BigDecimal;

public class LeaveRequestDurationResponse {
  private BigDecimal duration;

  public LeaveRequestDurationResponse(BigDecimal duration) {
    this.duration = duration;
  }

  public BigDecimal getDuration() {
    return duration;
  }
}
