package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.TSTimer;
import com.axelor.utils.api.ResponseStructure;

public class TSTimerResponse extends ResponseStructure {
  public TSTimerResponse(TSTimer timer) {
    super(timer.getVersion());
  }
}
