package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;

public interface TimesheetWorkflowCheckService {
  void confirmCheck(Timesheet timesheet) throws AxelorException;

  void validateCheck(Timesheet timesheet) throws AxelorException;

  void refuseCheck(Timesheet timesheet) throws AxelorException;

  void cancelCheck(Timesheet timesheet) throws AxelorException;
}
