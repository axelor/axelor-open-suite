package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.utils.api.ResponseStructure;

public class TimesheetResponse extends ResponseStructure {
  private Long timesheetId;

  public TimesheetResponse(Timesheet timesheet) {
    super(timesheet.getVersion());
    this.timesheetId = timesheet.getId();
  }

  public Long getTimesheetId() {
    return timesheetId;
  }
}
