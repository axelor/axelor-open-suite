package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface TimesheetLineCreateBusinessService {

  TimesheetLine createTimesheetLine(
      Project project,
      ProjectTask projectTask,
      Product product,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal duration,
      String comments,
      boolean toInvoice,
      ManufOrder manufOrder,
      OperationOrder operationOrder)
      throws AxelorException;
}
