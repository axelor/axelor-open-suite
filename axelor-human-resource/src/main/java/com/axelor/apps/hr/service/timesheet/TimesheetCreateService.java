package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TimesheetCreateService {
  Timesheet createTimesheet(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  List<Map<String, Object>> createDefaultLines(Timesheet timesheet);
}
