package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectPlanningTime;

public interface ProjectPlanningTimeToolService {
  Unit getDefaultTimeUnit(ProjectPlanningTime projectPlanningTime) throws AxelorException;
}
