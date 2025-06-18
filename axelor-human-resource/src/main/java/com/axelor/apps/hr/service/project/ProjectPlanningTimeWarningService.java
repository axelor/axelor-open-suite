package com.axelor.apps.hr.service.project;

import com.axelor.apps.project.db.ProjectTask;

public interface ProjectPlanningTimeWarningService {

  String getSprintWarning(ProjectTask projectTask);
}
