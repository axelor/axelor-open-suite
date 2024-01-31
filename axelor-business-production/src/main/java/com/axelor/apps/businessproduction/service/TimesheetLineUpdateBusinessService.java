package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface TimesheetLineUpdateBusinessService {
  void updateTimesheetLine(
      TimesheetLine timesheetLine,
      Project project,
      ProjectTask projectTask,
      BigDecimal duration,
      LocalDate date,
      String comments,
      Boolean toInvoice,
      ManufOrder manufOrder,
      OperationOrder operationOrder)
      throws AxelorException;
}
