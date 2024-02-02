package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;

public interface TimesheetRemoveService {
  void removeAfterToDateTimesheetLines(Timesheet timesheet);
}
