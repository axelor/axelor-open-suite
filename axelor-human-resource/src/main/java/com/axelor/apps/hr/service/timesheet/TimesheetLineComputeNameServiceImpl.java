package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.TimesheetLine;

public class TimesheetLineComputeNameServiceImpl implements TimesheetLineComputeNameService {

  @Override
  public void computeFullName(TimesheetLine timesheetLine) {
    timesheetLine.setFullName(
        timesheetLine.getTimesheet().getFullName()
            + " "
            + timesheetLine.getDate()
            + " "
            + timesheetLine.getId());
  }
}
