/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.TaskStatusProgressByCategoryRepository;
import com.axelor.apps.project.service.ProjectTaskServiceImpl;
import com.axelor.apps.project.service.TaskStatusToolService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectTaskHRServiceImpl extends ProjectTaskServiceImpl
    implements ProjectTaskHRService {

  @Inject
  public ProjectTaskHRServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      FrequencyRepository frequencyRepo,
      FrequencyService frequencyService,
      AppBaseService appBaseService,
      ProjectRepository projectRepository,
      AppProjectService appProjectService,
      TaskStatusToolService taskStatusToolService,
      TaskStatusProgressByCategoryRepository taskStatusProgressByCategoryRepository) {

    super(
        projectTaskRepo,
        frequencyRepo,
        frequencyService,
        appBaseService,
        projectRepository,
        appProjectService,
        taskStatusToolService,
        taskStatusProgressByCategoryRepository);
  }

  @Override
  public List<ProjectPlanningTime> updateProjectPlanningTime(
      ProjectTask projectTask, ProjectTask projectTaskDb) {

    List<ProjectPlanningTime> projectPlanningTimeList = projectTask.getProjectPlanningTimeList();

    if (CollectionUtils.isEmpty(projectPlanningTimeList)) {
      ProjectPlanningTime projectPlanningTime =
          createProjectPlanningTime(projectTask, projectTask.getSprint());

      if (projectPlanningTime != null) {
        projectPlanningTimeList.add(projectPlanningTime);
      }

      return projectPlanningTimeList;
    }

    if (isInvalidInput(projectTask) || isInvalidInput(projectTaskDb)) {
      return projectPlanningTimeList;
    }

    Employee employee =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getAssignedTo)
            .map(User::getEmployee)
            .orElse(null);
    Employee dbEmployee =
        Optional.ofNullable(projectTaskDb)
            .map(ProjectTask::getAssignedTo)
            .map(User::getEmployee)
            .orElse(null);
    SprintPeriod sprintPeriod = projectTask.getSprint().getSprintPeriod();
    SprintPeriod dbSprintPeriod = projectTaskDb.getSprint().getSprintPeriod();

    LocalDateTime fromDateTime =
        Optional.ofNullable(sprintPeriod.getFromDate()).map(LocalDate::atStartOfDay).orElse(null);
    LocalDateTime toDateTime =
        Optional.ofNullable(sprintPeriod.getToDate()).map(LocalDate::atStartOfDay).orElse(null);

    boolean updated =
        projectPlanningTimeList.stream()
                .filter(
                    planningTime ->
                        shouldUpdatePlanningTime(planningTime, dbEmployee, dbSprintPeriod))
                .peek(
                    planningTime ->
                        updateProjectPlanningTime(planningTime, employee, fromDateTime, toDateTime))
                .count()
            > 0;

    if (!updated) {
      ProjectPlanningTime projectPlanningTime =
          createProjectPlanningTime(projectTask, projectTask.getSprint());

      if (projectPlanningTime != null) {
        projectPlanningTimeList.add(projectPlanningTime);
      }
    }

    return projectPlanningTimeList;
  }

  private boolean isInvalidInput(ProjectTask projectTask) {

    return projectTask.getSprint() == null
        || projectTask.getSprint().getSprintPeriod() == null
        || projectTask.getAssignedTo().getEmployee() == null;
  }

  private boolean shouldUpdatePlanningTime(
      ProjectPlanningTime planningTime, Employee employee, SprintPeriod sprintPeriod) {

    Employee planningEmployee = planningTime.getEmployee();
    LocalDateTime startDateTime = planningTime.getStartDateTime();
    LocalDateTime endDateTime = planningTime.getEndDateTime();

    if (planningEmployee == null
        || startDateTime == null
        || endDateTime == null
        || sprintPeriod == null) {
      return false;
    }

    return planningEmployee.equals(employee)
        && startDateTime.toLocalDate().equals(sprintPeriod.getFromDate())
        && endDateTime.toLocalDate().equals(sprintPeriod.getToDate());
  }

  private void updateProjectPlanningTime(
      ProjectPlanningTime planningTime,
      Employee employee,
      LocalDateTime fromDateTime,
      LocalDateTime toDateTime) {

    planningTime.setEmployee(employee);
    planningTime.setStartDateTime(fromDateTime);
    planningTime.setEndDateTime(toDateTime);
  }

  @Override
  public ProjectPlanningTime createProjectPlanningTime(ProjectTask projectTask, Sprint sprint) {

    SprintPeriod sprintPeriod = sprint.getSprintPeriod();
    Employee assignedEmployee =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getAssignedTo)
            .map(User::getEmployee)
            .orElse(null);

    if (sprintPeriod == null || assignedEmployee == null) {
      return null;
    }

    List<ProjectPlanningTime> projectPlanningTimeList = projectTask.getProjectPlanningTimeList();

    if (CollectionUtils.isEmpty(projectPlanningTimeList)) {
      return createProjectPlanningTime(projectTask);
    }

    boolean planningTimeExists =
        projectPlanningTimeList.stream()
            .filter(p -> p.getEmployee() != null && p.getEmployee().equals(assignedEmployee))
            .anyMatch(
                p -> {
                  LocalDateTime startDateTime = p.getStartDateTime();
                  LocalDateTime endDateTime = p.getEndDateTime();
                  return startDateTime != null
                      && startDateTime.toLocalDate().equals(sprintPeriod.getFromDate())
                      && endDateTime != null
                      && endDateTime.toLocalDate().equals(sprintPeriod.getToDate());
                });

    if (!planningTimeExists) {
      return createProjectPlanningTime(projectTask);
    }

    return null;
  }

  protected ProjectPlanningTime createProjectPlanningTime(ProjectTask projectTask) {

    Employee employee =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getAssignedTo)
            .map(User::getEmployee)
            .orElse(null);

    SprintPeriod sprintPeriod =
        Optional.ofNullable(projectTask.getSprint()).map(Sprint::getSprintPeriod).orElse(null);

    if (employee == null
        || sprintPeriod == null
        || sprintPeriod.getFromDate() == null
        || sprintPeriod.getToDate() == null
        || projectTask.getProject() == null) {
      return null;
    }

    ProjectPlanningTime planningTime = new ProjectPlanningTime();

    planningTime.setProjectTask(projectTask);
    planningTime.setProject(projectRepository.find(projectTask.getProject().getId()));
    planningTime.setEmployee(employee);
    planningTime.setProduct(employee.getProduct());
    planningTime.setStartDateTime(sprintPeriod.getFromDate().atStartOfDay());
    planningTime.setEndDateTime(sprintPeriod.getToDate().atStartOfDay());

    return planningTime;
  }

  @Override
  public List<ProjectPlanningTime> getExistingPlanningTime(
      List<ProjectPlanningTime> projectPlanningTimeList,
      Employee employee,
      SprintPeriod sprintPeriod) {

    return Optional.ofNullable(projectPlanningTimeList).orElseGet(Collections::emptyList).stream()
        .filter(planningTime -> shouldUpdatePlanningTime(planningTime, employee, sprintPeriod))
        .collect(Collectors.toList());
  }
}
