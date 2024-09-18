package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.TimesheetLine;

public interface TimesheetLineComputeNameService {
  void computeFullName(TimesheetLine timesheetLine);
}
