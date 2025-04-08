package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.persist.Transactional;

public interface ProjectTaskPPTGenerateService {

  String getSprintOnChangeWarningWithoutSprint(ProjectTask projectTask);

  @Transactional
  void createUpdatePlanningTimeWithoutSprint(ProjectTask projectTask) throws AxelorException;
}
