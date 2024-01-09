package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;

public interface TimesheetProjectPlanningTimeService {
  void generateLinesFromExpectedProjectPlanning(Timesheet timesheet) throws AxelorException;
}
