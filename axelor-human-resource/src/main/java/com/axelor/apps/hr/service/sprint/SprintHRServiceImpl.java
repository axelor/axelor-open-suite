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
package com.axelor.apps.hr.service.sprint;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.project.ProjectTaskHRService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.sprint.SprintServiceImpl;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class SprintHRServiceImpl extends SprintServiceImpl {

  protected ProjectTaskHRService projectTaskHRService;

  @Inject
  public SprintHRServiceImpl(
      ProjectTaskRepository projectTaskRepo, ProjectTaskHRService projectTaskHRService) {

    super(projectTaskRepo);

    this.projectTaskHRService = projectTaskHRService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void attachTasksToSprint(Sprint sprint, List<ProjectTask> projectTasks) {

    if (CollectionUtils.isNotEmpty(projectTasks)) {

      for (ProjectTask task : projectTasks) {
        attachTaskToSprint(sprint, task);

        if (!sprint.getProjectTaskList().contains(task)) {
          projectTaskRepo.save(task);
        }
      }
    }
  }

  protected void attachTaskToSprint(Sprint sprint, ProjectTask task) {

    Employee assignedEmployee =
        Optional.ofNullable(task)
            .map(ProjectTask::getAssignedTo)
            .map(User::getEmployee)
            .orElse(null);
    SprintPeriod sprintPeriod = sprint.getSprintPeriod();
    Sprint currentSprint = task.getSprint();

    if (currentSprint == null || !currentSprint.equals(sprint)) {
      task.setSprint(sprint);
    }

    if (assignedEmployee == null || sprintPeriod == null) {
      return;
    }

    List<ProjectPlanningTime> projectPlanningTimeList =
        projectTaskHRService.getExistingPlanningTime(
            task.getProjectPlanningTimeList(),
            assignedEmployee,
            currentSprint != null ? currentSprint.getSprintPeriod() : null);

    if (CollectionUtils.isNotEmpty(projectPlanningTimeList)) {
      projectPlanningTimeList.forEach(
          planning -> {
            planning.setStartDateTime(sprintPeriod.getFromDate().atStartOfDay());
            planning.setEndDateTime(sprintPeriod.getToDate().atStartOfDay());
            BigDecimal plannedTime = projectTaskHRService.calculatePlannedTime(task);
            planning.setPlannedTime(plannedTime);
            planning.setDisplayPlannedTime(plannedTime);
          });
    } else {
      ProjectPlanningTime planningTime =
          projectTaskHRService.createProjectPlanningTime(task, sprint);

      if (planningTime != null) {
        task.addProjectPlanningTimeListItem(planningTime);
      }
    }
  }
}
