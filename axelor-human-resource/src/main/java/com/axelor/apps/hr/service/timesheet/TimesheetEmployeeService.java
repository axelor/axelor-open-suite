package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;

public interface TimesheetEmployeeService {
  Employee getEmployee(Project project) throws AxelorException;
}
