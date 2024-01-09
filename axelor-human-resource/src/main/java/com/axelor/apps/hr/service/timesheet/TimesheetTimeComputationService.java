package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.project.db.Project;
import java.math.BigDecimal;

public interface TimesheetTimeComputationService {
  void computeTimeSpent(Timesheet timesheet);

  BigDecimal computeSubTimeSpent(Project project);

  BigDecimal computeTimeSpent(Project project);
}
