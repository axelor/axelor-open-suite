package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface TimesheetLineUpdateService {
  void updateTimesheetLine(
      TimesheetLine timesheetLine,
      Project project,
      ProjectTask projectTask,
      Product product,
      BigDecimal duration,
      LocalDate date,
      String comments,
      Boolean toInvoice)
      throws AxelorException;
}
