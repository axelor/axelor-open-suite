package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.utils.api.ResponseStructure;

public class TimesheetLineResponse extends ResponseStructure {
  private Long timesheetLineId;

  public TimesheetLineResponse(TimesheetLine timesheetLine) {
    super(timesheetLine.getVersion());
    this.timesheetLineId = timesheetLine.getId();
  }

  public Long getTimesheetLineId() {
    return timesheetLineId;
  }
}
