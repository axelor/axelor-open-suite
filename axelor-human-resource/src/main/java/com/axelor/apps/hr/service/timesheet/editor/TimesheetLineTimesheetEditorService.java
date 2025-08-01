package com.axelor.apps.hr.service.timesheet.editor;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.ws.rs.core.Response;

public interface TimesheetLineTimesheetEditorService {

  /**
   * This service updates {@link TimesheetLine} by searching based on {@link Timesheet}, {@link
   * Project} and {@link ProjectTask} and creates new {@link TimesheetLine} or update existing with
   * difference of duration or hoursDuration
   */
  Response createOrUpdateTimesheetLine(
      Timesheet timesheet,
      Project project,
      ProjectTask projectTask,
      Product product,
      BigDecimal duration,
      BigDecimal hoursDuration,
      LocalDate date,
      String comments,
      Boolean toInvoice)
      throws AxelorException;

  void removeAllTimesheetLines(Timesheet timesheet);
}
