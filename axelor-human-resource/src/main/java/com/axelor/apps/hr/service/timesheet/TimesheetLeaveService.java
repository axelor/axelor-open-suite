package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;

public interface TimesheetLeaveService {
  void prefillLines(Timesheet timesheet) throws AxelorException;
}
