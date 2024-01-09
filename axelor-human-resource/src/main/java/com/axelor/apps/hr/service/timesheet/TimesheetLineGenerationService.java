package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.project.db.Project;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface TimesheetLineGenerationService {

  Timesheet generateLines(
      Timesheet timesheet,
      LocalDate fromGenerationDate,
      LocalDate toGenerationDate,
      BigDecimal logTime,
      Project project,
      Product product)
      throws AxelorException;

  void checkEmptyPeriod(Timesheet timesheet) throws AxelorException;
}
