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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.service.TaskTemplateServiceImpl;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class TaskTemplateHrServiceImpl extends TaskTemplateServiceImpl {

  protected ProjectPlanningTimeCreateService projectPlanningTimeCreateService;
  protected AppBaseService appBaseService;
  protected ProjectPlanningTimeComputeService projectPlanningTimeComputeService;
  protected AppProjectService appProjectService;
  protected ProjectPlanningTimeRepository projectPlanningTimeRepository;

  @Inject
  public TaskTemplateHrServiceImpl(
      ProjectPlanningTimeCreateService projectPlanningTimeCreateService,
      AppBaseService appBaseService,
      ProjectPlanningTimeComputeService projectPlanningTimeComputeService,
      AppProjectService appProjectService,
      ProjectPlanningTimeRepository projectPlanningTimeRepository) {
    this.projectPlanningTimeCreateService = projectPlanningTimeCreateService;
    this.appBaseService = appBaseService;
    this.projectPlanningTimeComputeService = projectPlanningTimeComputeService;
    this.appProjectService = appProjectService;
    this.projectPlanningTimeRepository = projectPlanningTimeRepository;
  }

  @Override
  public void manageTemplateFields(ProjectTask task, TaskTemplate taskTemplate, Project project)
      throws AxelorException {
    super.manageTemplateFields(task, taskTemplate, project);
    task.setTimeUnit(appBaseService.getUnitHours());

    if (Optional.ofNullable(appProjectService.getAppProject())
        .map(AppProject::getEnablePlanification)
        .orElse(false)) {
      manageProjectPlanningTime(taskTemplate, task, project);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void manageProjectPlanningTime(
      TaskTemplate taskTemplate, ProjectTask task, Project project) throws AxelorException {
    task.setTotalPlannedHrs(taskTemplate.getTotalPlannedHrs());

    if (task.getTotalPlannedHrs().signum() > 0
        && task.getAssignedTo() != null
        && task.getProduct() != null) {
      LocalDateTime startDateTime = task.getTaskDate().atStartOfDay();
      BigDecimal dailyWorkHours = appBaseService.getDailyWorkHours();
      Unit unitHours = appBaseService.getUnitHours();
      ProjectPlanningTime projectPlanningTime =
          projectPlanningTimeCreateService.createProjectPlanningTime(
              startDateTime,
              task,
              project,
              100,
              task.getAssignedTo().getEmployee(),
              task.getProduct(),
              dailyWorkHours,
              startDateTime,
              task.getSite(),
              unitHours);

      projectPlanningTime.setDisplayTimeUnit(unitHours);
      projectPlanningTime.setDisplayPlannedTime(task.getTotalPlannedHrs());
      projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
      projectPlanningTimeRepository.save(projectPlanningTime);
    }
  }
}
