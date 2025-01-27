package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectTask;

public interface ProjectTaskSprintService {
  String getSprintOnChangeWarning(ProjectTask projectTask);

  ProjectTask createOrMovePlanification(ProjectTask projectTask) throws AxelorException;
}
