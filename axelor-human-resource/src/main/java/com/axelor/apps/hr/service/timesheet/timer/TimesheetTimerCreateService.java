package com.axelor.apps.hr.service.timesheet.timer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import java.time.LocalDateTime;

public interface TimesheetTimerCreateService {

  TSTimer createOrUpdateTimer(
      Employee employee,
      Project project,
      ProjectTask projectTask,
      Product product,
      Long duration,
      String comment,
      LocalDateTime startDateTime)
      throws AxelorException;

  TSTimer createOrUpdateTimer(
      Project project,
      ProjectTask projectTask,
      Product product,
      Long duration,
      String comment,
      LocalDateTime startDateTime)
      throws AxelorException;

  TSTimer createTSTimer(
      Employee employee,
      Project project,
      ProjectTask projectTask,
      Product product,
      Long duration,
      String comment,
      LocalDateTime startDateTime)
      throws AxelorException;

  TSTimer updateTimer(
      TSTimer timer,
      Employee employee,
      Project project,
      ProjectTask projectTask,
      Product product,
      Long duration,
      String comment,
      LocalDateTime startDateTime)
      throws AxelorException;

  void resetTimer(TSTimer timer);
}
