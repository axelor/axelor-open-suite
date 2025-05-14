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
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
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
  protected ProjectTaskRepository projectTaskRepository;
  protected ProjectPlanningTimeRepository projectPlanningTimeRepository;

  @Inject
  public ProjectTaskSprintServiceImpl(
      AppProjectService appProjectService,
      ProjectPlanningTimeComputeService projectPlanningTimeComputeService,
      ProjectPlanningTimeCreateService projectPlanningTimeCreateService,
      ProjectTaskRepository projectTaskRepository,
      ProjectPlanningTimeRepository projectPlanningTimeRepository) {
    this.appProjectService = appProjectService;
    this.projectPlanningTimeComputeService = projectPlanningTimeComputeService;
    this.projectPlanningTimeCreateService = projectPlanningTimeCreateService;
    this.projectTaskRepository = projectTaskRepository;
    this.projectPlanningTimeRepository = projectPlanningTimeRepository;
  }

  @Override
  public String getSprintOnChangeWarning(ProjectTask projectTask) {
    if (validateConfigAndSprint(projectTask) == null) {
      return "";
    }

    Sprint savedSprint = getOldActiveSprint(projectTask);
    BigDecimal oldBudgetedTime = getOldBudgetedTime(projectTask);

    Sprint backlogSprint =
        Optional.of(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getBacklogSprint)
            .orElse(null);

    if (projectTask.getActiveSprint().equals(backlogSprint)) {
      return "";
    }

    Set<ProjectPlanningTime> projectPlanningTimeSet =
        getProjectPlanningTimeOnOldSprint(projectTask, savedSprint);

    String warning =
        getBudgetedTimeOnChangeWarning(projectPlanningTimeSet, oldBudgetedTime, projectTask);
    if (StringUtils.notEmpty(warning)) {
      return warning;
    }

    if (ObjectUtils.isEmpty(projectPlanningTimeSet)
        || savedSprint == null
        || savedSprint.equals(backlogSprint)
        || savedSprint.equals(projectTask.getActiveSprint())) {
      if (ObjectUtils.isEmpty(projectTask.getProjectPlanningTimeList())) {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_FIRST_REQUEST);
      } else {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_NEW_REQUEST);
      }
    } else {
      return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_EXISTING_ON_OLD_SPRINT);
    }
  }

  protected String getBudgetedTimeOnChangeWarning(
      Set<ProjectPlanningTime> projectPlanningTimeSet,
      BigDecimal oldBudgetedTime,
      ProjectTask projectTask) {
    if (projectTask.getBudgetedTime().signum() == 0
        || projectTask.getBudgetedTime().compareTo(oldBudgetedTime) == 0
        || ObjectUtils.isEmpty(projectPlanningTimeSet)) {
      return "";
    }

    projectPlanningTimeSet =
        projectPlanningTimeSet.stream()
            .filter(ppt -> ppt.getDisplayPlannedTime().compareTo(oldBudgetedTime) == 0)
            .collect(Collectors.toSet());

    if (ObjectUtils.isEmpty(projectPlanningTimeSet)) {
      if (ObjectUtils.isEmpty(projectTask.getProjectPlanningTimeList())) {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_FIRST_REQUEST);
      } else {
        return I18n.get(HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_NEW_REQUEST);
      }

    } else {
      return I18n.get(
          HumanResourceExceptionMessage.PROJECT_PLANNING_TIME_EXISTING_WITH_OLD_DURATION);
    }
  }

  @Override
  public void createOrMovePlanification(ProjectTask projectTask) throws AxelorException {
    if (validateConfigAndSprint(projectTask) == null) {
      return;
    }

    Sprint savedSprint = getOldActiveSprint(projectTask);
    BigDecimal oldBudgetedTime = getOldBudgetedTime(projectTask);

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

  protected Sprint validateConfigAndSprint(ProjectTask projectTask) {
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

  protected Set<ProjectPlanningTime> getProjectPlanningTimeOnOldSprint(
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

  protected void moveProjectPlanningTime(
      ProjectPlanningTime projectPlanningTime, ProjectTask projectTask) throws AxelorException {
    Sprint activeSprint = projectTask.getActiveSprint();
    if (activeSprint == null
        || activeSprint.getFromDate() == null
        || activeSprint.getToDate() == null) {
      return;
    }

    updateProjectPlanningTimeDatesAndDuration(projectPlanningTime, projectTask);
  }

  protected void createProjectPlanningTime(ProjectTask projectTask) throws AxelorException {
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
            getTimeUnit(projectTask));

    updateProjectPlanningTimeDatesAndDuration(projectPlanningTime, projectTask);
    projectTask.addProjectPlanningTimeListItem(projectPlanningTime);
  }

  protected Unit getTimeUnit(ProjectTask projectTask) {
    Unit unit = projectTask.getTimeUnit();
    if (unit == null) {
      unit =
          Optional.of(projectTask)
              .map(ProjectTask::getProject)
              .map(Project::getProjectTimeUnit)
              .orElse(null);
    }

    return unit;
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
    Unit timeUnit = getTimeUnit(projectTask);
    if (timeUnit != null) {
      projectPlanningTime.setDisplayTimeUnit(timeUnit);
    }

    projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
    projectPlanningTime.setEndDateTime(activeSprint.getToDate().atTime(23, 59));
  }

  protected Sprint getOldActiveSprint(ProjectTask projectTask) {
    Sprint savedSprint = projectTask.getOldActiveSprint();
    if (savedSprint == null && projectTask.getId() != null) {
      savedSprint = projectTaskRepository.find(projectTask.getId()).getActiveSprint();
    }

    return savedSprint;
  }

  protected BigDecimal getOldBudgetedTime(ProjectTask projectTask) {
    BigDecimal oldBudgetedTime = projectTask.getOldBudgetedTime();
    if ((oldBudgetedTime == null || oldBudgetedTime.signum() == 0) && projectTask.getId() != null) {
      oldBudgetedTime = projectTaskRepository.find(projectTask.getId()).getBudgetedTime();
    }

    return oldBudgetedTime;
  }
}
