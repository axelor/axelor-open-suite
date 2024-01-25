package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import java.time.LocalDate;

public interface TimesheetFetchService {
  Timesheet getDraftTimesheet(Employee employee, LocalDate date);

  Timesheet getCurrentOrCreateTimesheet() throws AxelorException;

  Timesheet getOrCreateOpenTimesheet(Employee employee, LocalDate date) throws AxelorException;
}
