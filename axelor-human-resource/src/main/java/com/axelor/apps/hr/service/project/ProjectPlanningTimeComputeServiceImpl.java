/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.apps.project.service.config.ProjectConfigService;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class ProjectPlanningTimeComputeServiceImpl implements ProjectPlanningTimeComputeService {

  protected ProjectPlanningTimeService projectPlanningTimeService;
  protected ProjectConfigService projectConfigService;
  protected PlannedTimeValueService plannedTimeValueService;
  protected AppBaseService appBaseService;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected ProjectPlanningTimeToolService projectPlanningTimeToolService;

  @Inject
  public ProjectPlanningTimeComputeServiceImpl(
      ProjectPlanningTimeService projectPlanningTimeService,
      ProjectConfigService projectConfigService,
      PlannedTimeValueService plannedTimeValueService,
      AppBaseService appBaseService,
      UnitConversionForProjectService unitConversionForProjectService,
      ProjectPlanningTimeToolService projectPlanningTimeToolService) {
    this.projectPlanningTimeService = projectPlanningTimeService;
    this.projectConfigService = projectConfigService;
    this.plannedTimeValueService = plannedTimeValueService;
    this.appBaseService = appBaseService;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.projectPlanningTimeToolService = projectPlanningTimeToolService;
  }

  @Override
  public void computePlannedTimeValues(ProjectPlanningTime projectPlanningTime)
      throws AxelorException {
    if (projectPlanningTime == null) {
      return;
    }

    Unit timeUnit = projectPlanningTimeToolService.getDefaultTimeUnit(projectPlanningTime);
    projectPlanningTime.setTimeUnit(timeUnit);
    projectPlanningTime.setDisplayTimeUnit(timeUnit);

    projectPlanningTime.setPlannedTime(
        projectPlanningTimeService.computePlannedTime(projectPlanningTime));

    Project project = getProject(projectPlanningTime);
    Company company = Optional.ofNullable(project).map(Project::getCompany).orElse(null);

    if (company != null
        && projectConfigService.getProjectConfig(company).getIsSelectionOnDisplayPlannedTime()) {
      if (projectPlanningTime.getDisplayPlannedTimeRestricted() != null) {
        projectPlanningTime.setDisplayPlannedTime(
            Optional.of(projectPlanningTime)
                .map(ProjectPlanningTime::getDisplayPlannedTimeRestricted)
                .map(PlannedTimeValue::getPlannedTime)
                .orElse(BigDecimal.ZERO));
      }
    } else {
      projectPlanningTime.setDisplayPlannedTimeRestricted(
          plannedTimeValueService.createPlannedTimeValue(
              projectPlanningTime.getDisplayPlannedTime()));
    }

    projectPlanningTime.setEndDateTime(computeEndDateTime(projectPlanningTime, project));
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

  @Override
  public LocalDateTime computeEndDateTime(ProjectPlanningTime projectPlanningTime, Project project)
      throws AxelorException {
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
