package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.service.UnitConversionForProjectService;
import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.config.ProjectConfigService;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProjectPlanningTimeComputeServiceImpl implements ProjectPlanningTimeComputeService {

  protected ProjectPlanningTimeService projectPlanningTimeService;
  protected ProjectConfigService projectConfigService;
  protected PlannedTimeValueService plannedTimeValueService;
  protected AppBaseService appBaseService;
  protected UnitConversionForProjectService unitConversionForProjectService;

  @Inject
  public ProjectPlanningTimeComputeServiceImpl(
      ProjectPlanningTimeService projectPlanningTimeService,
      ProjectConfigService projectConfigService,
      PlannedTimeValueService plannedTimeValueService,
      AppBaseService appBaseService,
      UnitConversionForProjectService unitConversionForProjectService) {
    this.projectPlanningTimeService = projectPlanningTimeService;
    this.projectConfigService = projectConfigService;
    this.plannedTimeValueService = plannedTimeValueService;
    this.appBaseService = appBaseService;
    this.unitConversionForProjectService = unitConversionForProjectService;
  }

  @Override
  public Map<String, Object> computePlannedTimeValues(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();
    if (projectPlanningTime == null) {
      return valuesMap;
    }
    projectPlanningTime.setPlannedTime(
        projectPlanningTimeService.computePlannedTime(projectPlanningTime));
    valuesMap.put("plannedTime", projectPlanningTime.getPlannedTime());

    Project project = getProject(projectPlanningTime);
    Company company = Optional.ofNullable(project).map(Project::getCompany).orElse(null);

    if (company != null
        && projectConfigService.getProjectConfig(company).getIsSelectionOnDisplayPlannedTime()) {
      if (projectPlanningTime.getDisplayPlannedTimeRestricted() != null) {
        valuesMap.put(
            "displayPlannedTime",
            Optional.of(projectPlanningTime)
                .map(ProjectPlanningTime::getDisplayPlannedTimeRestricted)
                .map(PlannedTimeValue::getPlannedTime)
                .orElse(BigDecimal.ZERO));
      }
    } else {
      valuesMap.put(
          "displayPlannedTimeRestricted",
          plannedTimeValueService.createPlannedTimeValue(
              projectPlanningTime.getDisplayPlannedTime()));
    }

    valuesMap.put("endDateTime", computeEndDateTime(projectPlanningTime, project));

    return valuesMap;
  }

  protected Project getProject(ProjectPlanningTime projectPlanningTime) {
    Project project =
        Optional.ofNullable(projectPlanningTime)
            .map(ProjectPlanningTime::getProject)
            .orElse(
                Optional.ofNullable(projectPlanningTime)
                    .map(ProjectPlanningTime::getProjectTask)
                    .map(ProjectTask::getProject)
                    .orElse(null));

    return project;
  }

  protected LocalDateTime computeEndDateTime(
      ProjectPlanningTime projectPlanningTime, Project project) throws AxelorException {
    if (projectPlanningTime == null || projectPlanningTime.getStartDateTime() == null) {
      return null;
    }

    AppBase appBase = appBaseService.getAppBase();
    BigDecimal timeInDays = BigDecimal.ZERO;
    if (projectPlanningTime.getTimeUnit() == null || appBase == null) {
      return projectPlanningTime.getStartDateTime();
    }

    if (projectPlanningTime.getTimeUnit() == appBase.getUnitDays()) {
      timeInDays = projectPlanningTime.getPlannedTime();
    } else if (projectPlanningTime.getTimeUnit() == appBase.getUnitHours()) {
      timeInDays =
          unitConversionForProjectService.convert(
              projectPlanningTime.getTimeUnit(),
              appBase.getUnitDays(),
              projectPlanningTime.getPlannedTime(),
              AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
              project);
    }

    return projectPlanningTime
        .getStartDateTime()
        .plusHours(timeInDays.multiply(new BigDecimal(24)).longValue());
  }
}
