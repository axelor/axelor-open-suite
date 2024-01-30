package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.TSTimer;
import com.axelor.utils.api.ResponseStructure;

public class TSTimerResponse extends ResponseStructure {

  private Long timerId;

  public TSTimerResponse(TSTimer timer) {
    super(timer.getVersion());
    this.timerId = timer.getId();
  }

  public Long getTimerId() {
    return timerId;
  }
}
