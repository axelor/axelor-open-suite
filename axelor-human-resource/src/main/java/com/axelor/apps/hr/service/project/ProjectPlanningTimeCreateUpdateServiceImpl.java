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
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectPlanningTimeCreateUpdateServiceImpl
    implements ProjectPlanningTimeCreateUpdateService {

  protected ProjectTaskRepository projectTaskRepository;
  protected ProjectPlanningTimeService projectPlanningTimeService;
  protected ProjectTaskSprintService projectTaskSprintService;
  protected ProjectTaskPPTGenerateService projectTaskPPTGenerateService;
  protected ProjectPlanningTimeCreateService projectPlanningTimeCreateService;
  protected ProjectPlanningTimeComputeService projectPlanningTimeComputeService;
  protected AppProjectService appProjectService;

  @Inject
  public ProjectPlanningTimeCreateUpdateServiceImpl(
      ProjectTaskRepository projectTaskRepository,
      ProjectPlanningTimeService projectPlanningTimeService,
      ProjectTaskSprintService projectTaskSprintService,
      ProjectTaskPPTGenerateService projectTaskPPTGenerateService,
      ProjectPlanningTimeComputeService projectPlanningTimeComputeService,
      ProjectPlanningTimeCreateService projectPlanningTimeCreateService,
      AppProjectService appProjectService) {
    this.projectTaskRepository = projectTaskRepository;
    this.projectPlanningTimeService = projectPlanningTimeService;
    this.projectTaskSprintService = projectTaskSprintService;
    this.projectTaskPPTGenerateService = projectTaskPPTGenerateService;
    this.projectPlanningTimeComputeService = projectPlanningTimeComputeService;
    this.projectPlanningTimeCreateService = projectPlanningTimeCreateService;
    this.appProjectService = appProjectService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createOrMovePlannification(ProjectTask projectTask) throws AxelorException {
    if (projectTask.getActiveSprint() != null) {
      createOrMovePlanificationWithSprint(projectTask);
    } else {
      if (!appProjectService.getAppProject().getBlockPPTGeneration()) {
        createUpdatePlanningTimeWithoutSprint(projectTask);
      }
    }
  }

  protected void createUpdatePlanningTimeWithoutSprint(ProjectTask projectTask)
      throws AxelorException {

    Set<ProjectPlanningTime> projectPlanningTimeSet =
        projectTaskPPTGenerateService.getProjectPlanningTimeOnOldDuration(
            projectTask,
            projectTask.getTaskDate(),
            projectPlanningTimeService.getOldBudgetedTime(projectTask));
    if (projectPlanningTimeSet.size() == 1) {
      projectTaskPPTGenerateService.updateProjectPlanningTimeDatesAndDurationWithoutSprint(
          projectPlanningTimeSet.stream().findFirst().get(), projectTask);
    }
    if (CollectionUtils.isEmpty(projectPlanningTimeSet)) {
      createPlanningTime(projectTask);
    }
  }

  protected void createPlanningTime(ProjectTask projectTask) throws AxelorException {
    LocalDate startDateTime = projectTask.getTaskDate();
    Optional<Employee> employee =
        Optional.of(projectTask).map(ProjectTask::getAssignedTo).map(User::getEmployee);
    if (startDateTime == null || employee.isEmpty()) {
      return;
    }
    ProjectPlanningTime projectPlanningTime =
        projectPlanningTimeCreateService.createProjectPlanningTime(
            startDateTime.atStartOfDay(),
            projectTask,
            projectTask.getProject(),
            100,
            employee.get(),
            projectTask.getProduct(),
            employee.get().getDailyWorkHours(),
            null,
            projectTask.getSite(),
            projectPlanningTimeService.getTimeUnit(projectTask));
    LocalDateTime taskEndDateTime =
        projectPlanningTimeComputeService.computeEndDateTime(
            projectPlanningTime, projectTask.getProject());
    projectPlanningTime.setEndDateTime(taskEndDateTime);

    projectPlanningTime.setDisplayPlannedTime(projectTask.getBudgetedTime());
    Unit timeUnit = projectPlanningTimeService.getTimeUnit(projectTask);
    if (timeUnit != null) {
      projectPlanningTime.setDisplayTimeUnit(timeUnit);
    }

    projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
    projectTask.addProjectPlanningTimeListItem(projectPlanningTime);
  }

  protected void createOrMovePlanificationWithSprint(ProjectTask projectTask)
      throws AxelorException {
    if (projectTaskSprintService.validateConfigAndSprint(projectTask) == null) {
      return;
    }

    Sprint savedSprint = projectTaskSprintService.getOldActiveSprint(projectTask);
    BigDecimal oldBudgetedTime = projectPlanningTimeService.getOldBudgetedTime(projectTask);

    Sprint backlogSprint =
        Optional.of(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getBacklogSprint)
            .orElse(null);

    if (projectTask.getActiveSprint().equals(backlogSprint)) {
      return;
    }

    Set<ProjectPlanningTime> projectPlanningTimeSet =
        projectTaskSprintService.getProjectPlanningTimeOnOldSprint(projectTask, savedSprint);

    if (projectTask.getActiveSprint().equals(savedSprint)
        && projectTask.getBudgetedTime().compareTo(oldBudgetedTime) != 0) {
      projectPlanningTimeSet =
          projectPlanningTimeSet.stream()
              .filter(ppt -> ppt.getDisplayPlannedTime().compareTo(oldBudgetedTime) == 0)
              .collect(Collectors.toSet());
    }

    if (savedSprint != null
        && !savedSprint.equals(backlogSprint)
        && !ObjectUtils.isEmpty(projectPlanningTimeSet)) {
      for (ProjectPlanningTime projectPlanningTime : projectPlanningTimeSet) {
        projectTaskSprintService.moveProjectPlanningTime(projectPlanningTime, projectTask);
      }

      return;
    }

    projectTaskSprintService.createProjectPlanningTime(projectTask);
  }
}
