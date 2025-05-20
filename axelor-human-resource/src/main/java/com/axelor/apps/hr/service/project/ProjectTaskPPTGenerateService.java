package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface ProjectTaskPPTGenerateService {

  void createUpdatePlanningTimeWithoutSprint(ProjectTask projectTask) throws AxelorException;

  void updateProjectPlanningTimeDatesAndDurationWithoutSprint(
      ProjectPlanningTime projectPlanningTime, ProjectTask projectTask) throws AxelorException;

  Set<ProjectPlanningTime> getProjectPlanningTimeOnOldDuration(
      ProjectTask projectTask, LocalDate fromDate, BigDecimal duration);
}
