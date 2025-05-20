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
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectTaskSprintServiceImpl implements ProjectTaskSprintService {

  protected AppProjectService appProjectService;
  protected ProjectPlanningTimeComputeService projectPlanningTimeComputeService;
  protected ProjectPlanningTimeCreateService projectPlanningTimeCreateService;
  protected ProjectPlanningTimeService projectPlanningTimeService;
  protected ProjectTaskRepository projectTaskRepository;
  protected ProjectPlanningTimeRepository projectPlanningTimeRepository;

  @Inject
  public ProjectTaskSprintServiceImpl(
      AppProjectService appProjectService,
      ProjectPlanningTimeComputeService projectPlanningTimeComputeService,
      ProjectPlanningTimeCreateService projectPlanningTimeCreateService,
      ProjectPlanningTimeService projectPlanningTimeService,
      ProjectTaskRepository projectTaskRepository,
      ProjectPlanningTimeRepository projectPlanningTimeRepository) {
    this.appProjectService = appProjectService;
    this.projectPlanningTimeComputeService = projectPlanningTimeComputeService;
    this.projectPlanningTimeCreateService = projectPlanningTimeCreateService;
    this.projectPlanningTimeService = projectPlanningTimeService;
    this.projectTaskRepository = projectTaskRepository;
    this.projectPlanningTimeRepository = projectPlanningTimeRepository;
  }

  @Override
  public void createOrMovePlanification(ProjectTask projectTask) throws AxelorException {
    if (validateConfigAndSprint(projectTask) == null) {
      return;
    }

    Sprint savedSprint = getOldActiveSprint(projectTask);
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
        getProjectPlanningTimeOnOldSprint(projectTask, savedSprint);

    if (projectTask.getActiveSprint().equals(savedSprint)
        && projectTask.getBudgetedTime().compareTo(oldBudgetedTime) != 0) {
      projectPlanningTimeSet =
          projectPlanningTimeSet.stream()
              .filter(ppt -> ppt.getDisplayPlannedTime().compareTo(oldBudgetedTime) == 0)
              .collect(Collectors.toSet());
    }

    if (savedSprint != null && !savedSprint.equals(backlogSprint)) {
      if (!ObjectUtils.isEmpty(projectPlanningTimeSet)) {
        for (ProjectPlanningTime projectPlanningTime : projectPlanningTimeSet) {
          moveProjectPlanningTime(projectPlanningTime, projectTask);
        }

        return;
      }
    }
    createProjectPlanningTime(projectTask);
  }

  @Override
  public Sprint validateConfigAndSprint(ProjectTask projectTask) {
    Sprint currentSprint =
        Optional.ofNullable(projectTask).map(ProjectTask::getActiveSprint).orElse(null);
    return Optional.ofNullable(appProjectService.getAppProject())
                .map(AppProject::getEnablePlanification)
                .orElse(false)
            && Optional.ofNullable(projectTask)
                    .map(ProjectTask::getBudgetedTime)
                    .map(BigDecimal::signum)
                    .orElse(0)
                > 0
        ? currentSprint
        : null;
  }

  @Override
  public Set<ProjectPlanningTime> getProjectPlanningTimeOnOldSprint(
      ProjectTask projectTask, Sprint savedSprint) {
    Set<ProjectPlanningTime> projectPlanningTimeSet = new HashSet<>();
    if (savedSprint != null && !ObjectUtils.isEmpty(projectTask.getProjectPlanningTimeList())) {
      LocalDate fromDate = savedSprint.getFromDate();
      LocalDate toDate = savedSprint.getToDate();
      if (fromDate != null && toDate != null) {
        return projectTask.getProjectPlanningTimeList().stream()
            .filter(
                ppt ->
                    ppt.getStartDateTime().toLocalDate().equals(fromDate)
                        && ppt.getEndDateTime().toLocalDate().equals(toDate))
            .collect(Collectors.toSet());
      }
    }

    return projectPlanningTimeSet;
  }

  @Override
  public void moveProjectPlanningTime(
      ProjectPlanningTime projectPlanningTime, ProjectTask projectTask) throws AxelorException {
    Sprint activeSprint = projectTask.getActiveSprint();
    if (activeSprint == null
        || activeSprint.getFromDate() == null
        || activeSprint.getToDate() == null) {
      return;
    }

    updateProjectPlanningTimeDatesAndDuration(projectPlanningTime, projectTask);
  }

  @Override
  public void createProjectPlanningTime(ProjectTask projectTask) throws AxelorException {
    Optional<LocalDateTime> startDateTime =
        Optional.of(projectTask)
            .map(ProjectTask::getActiveSprint)
            .map(Sprint::getFromDate)
            .map(LocalDate::atStartOfDay);
    Optional<LocalDateTime> endDateTime =
        Optional.of(projectTask)
            .map(ProjectTask::getActiveSprint)
            .map(Sprint::getToDate)
            .map(date -> date.atTime(23, 59));
    Optional<Employee> employee =
        Optional.of(projectTask).map(ProjectTask::getAssignedTo).map(User::getEmployee);

    if (startDateTime.isEmpty() || endDateTime.isEmpty() || employee.isEmpty()) {
      return;
    }

    ProjectPlanningTime projectPlanningTime =
        projectPlanningTimeCreateService.createProjectPlanningTime(
            startDateTime.get(),
            projectTask,
            projectTask.getProject(),
            100,
            employee.get(),
            projectTask.getProduct(),
            employee.get().getDailyWorkHours(),
            endDateTime.get(),
            projectTask.getSite(),
            projectPlanningTimeService.getTimeUnit(projectTask));

    updateProjectPlanningTimeDatesAndDuration(projectPlanningTime, projectTask);
    projectTask.addProjectPlanningTimeListItem(projectPlanningTime);
  }

  protected void updateProjectPlanningTimeDatesAndDuration(
      ProjectPlanningTime projectPlanningTime, ProjectTask projectTask) throws AxelorException {
    Sprint activeSprint = projectTask.getActiveSprint();
    if (activeSprint == null
        || activeSprint.getFromDate() == null
        || activeSprint.getToDate() == null) {
      return;
    }

    projectPlanningTime.setStartDateTime(activeSprint.getFromDate().atStartOfDay());
    projectPlanningTime.setEndDateTime(activeSprint.getToDate().atTime(23, 59));
    projectPlanningTime.setDisplayPlannedTime(projectTask.getBudgetedTime());
    Unit timeUnit = projectPlanningTimeService.getTimeUnit(projectTask);
    if (timeUnit != null) {
      projectPlanningTime.setDisplayTimeUnit(timeUnit);
    }

    projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
    projectPlanningTime.setEndDateTime(activeSprint.getToDate().atTime(23, 59));
  }

  @Override
  public Sprint getOldActiveSprint(ProjectTask projectTask) {
    Sprint savedSprint = projectTask.getOldActiveSprint();
    if (savedSprint == null && projectTask.getId() != null) {
      savedSprint = projectTaskRepository.find(projectTask.getId()).getActiveSprint();
    }

    return savedSprint;
  }
}
