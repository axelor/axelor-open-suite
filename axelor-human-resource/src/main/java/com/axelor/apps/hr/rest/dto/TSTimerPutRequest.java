package com.axelor.apps.hr.rest.dto;

import javax.validation.constraints.Min;

public class TSTimerPutRequest {

  @Min(0)
  private Long duration;

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }
}
