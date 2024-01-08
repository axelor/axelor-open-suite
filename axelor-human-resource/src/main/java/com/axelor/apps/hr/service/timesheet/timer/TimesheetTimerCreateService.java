package com.axelor.apps.hr.service.timesheet.timer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;

public interface TimesheetTimerCreateService {

  TSTimer createOrUpdateTimer(
      Employee employee, Project project, ProjectTask projectTask, Product product)
      throws AxelorException;

  TSTimer createTSTimer(
      Employee employee, Project project, ProjectTask projectTask, Product product)
      throws AxelorException;

  TSTimer updateTimer(
      TSTimer tsTimer,
      Employee employee,
      Project project,
      ProjectTask projectTask,
      Product product);
}
