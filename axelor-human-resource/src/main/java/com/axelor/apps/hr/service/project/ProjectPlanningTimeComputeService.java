package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectPlanningTime;
import java.util.Map;

public interface ProjectPlanningTimeComputeService {
  Map<String, Object> computePlannedTimeValues(ProjectPlanningTime projectPlanningTime)
      throws AxelorException;
}
