package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.db.Query;
import java.time.LocalDate;

public interface TimesheetQueryService {

  Query<Timesheet> getTimesheetQuery(TimesheetLine timesheetLine);

  Query<Timesheet> getTimesheetQuery(Employee employee, Company company, LocalDate date);
}
