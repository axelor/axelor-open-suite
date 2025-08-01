package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TimesheetLineDeleteRequest extends RequestPostStructure {

  @Min(0)
  @NotNull
  private Long timesheetId;

  public Long getTimesheetId() {
    return timesheetId;
  }

  public void setTimesheetId(Long timesheetId) {
    this.timesheetId = timesheetId;
  }

  public Timesheet fetchTimesheet() {
    return ObjectFinder.find(Timesheet.class, timesheetId, ObjectFinder.NO_VERSION);
  }
}
