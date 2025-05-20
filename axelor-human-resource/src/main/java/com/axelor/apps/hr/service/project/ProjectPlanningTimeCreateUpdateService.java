package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectTask;

public interface ProjectPlanningTimeCreateUpdateService {

  void createOrMovePlannification(ProjectTask projectTask) throws AxelorException;
}
