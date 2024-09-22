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
package com.axelor.apps.businessproject.service.sprint;

import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SprintServiceImpl implements SprintService {

  public SprintRepository sprintRepo;
  public ProjectTaskRepository projectTaskRepo;
  public ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  public SprintAllocationLineService sprintAllocationLineService;

  @Inject
  public SprintServiceImpl(
      SprintRepository sprintRepo,
      ProjectTaskRepository projectTaskRepo,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      SprintAllocationLineService sprintAllocationLineService) {

    this.sprintRepo = sprintRepo;
    this.projectTaskRepo = projectTaskRepo;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.sprintAllocationLineService = sprintAllocationLineService;
  }

  @Override
  public String sprintPeriodDomain(Set<Project> projects) {

    if (CollectionUtils.isEmpty(projects)) {
      return "self.id in (0)";
    }

    String companyIds =
        projects.stream()
            .filter(project -> project.getCompany() != null)
            .map(project -> project.getCompany().getId().toString())
            .distinct()
            .collect(Collectors.joining(","));

    return "self.company.id in (" + companyIds + ") and self.toDate >= '" + LocalDate.now() + "'";
  }

  @Override
  @Transactional
  public List<Sprint> generateSprints(Set<Project> projects, Set<SprintPeriod> sprintPeriods) {

    List<Sprint> sprintList = new ArrayList<>();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

    projects.forEach(
        project -> {
          sprintPeriods.forEach(
              sprintPeriod -> {
                Sprint sprint = new Sprint();
                sprint.setName(
                    I18n.get("Sprint")
                        + " "
                        + formatter.format(sprintPeriod.getFromDate())
                        + " - "
                        + formatter.format(sprintPeriod.getToDate()));
                sprint.setSprintPeriod(sprintPeriod);
                sprint.setProject(project);
                sprintRepo.save(sprint);
                sprintList.add(sprint);
              });
        });

    return sprintList;
  }

  @Override
  @Transactional
  public void attachTasksToSprint(Sprint sprint, List<ProjectTask> projectTasks) {

    if (sprint.getSprintPeriod() == null) {
      return;
    }

    for (ProjectTask task : projectTasks) {
      Sprint currentSprint = task.getSprint();
      Employee assignedEmployee = task.getAssignedTo().getEmployee();
      LocalDateTime sprintStart = sprint.getSprintPeriod().getFromDate().atStartOfDay();
      LocalDateTime sprintEnd = sprint.getSprintPeriod().getToDate().atStartOfDay();

      if (task.getTimeUnit() == null || assignedEmployee == null) {
        continue;
      }

      task.setSprint(sprint);

      List<ProjectPlanningTime> projectPlanningTimeList =
          projectTaskBusinessProjectService.getExistingPlanningTime(
              task.getProjectPlanningTimeList(),
              assignedEmployee,
              currentSprint != null ? currentSprint.getSprintPeriod() : null);

      if (CollectionUtils.isNotEmpty(projectPlanningTimeList)) {
        projectPlanningTimeList.forEach(
            planning -> {
              planning.setStartDateTime(sprintStart);
              planning.setEndDateTime(sprintEnd);
            });
      } else {
        ProjectPlanningTime planningTime =
            projectTaskBusinessProjectService.createProjectPlanningTime(task, sprint);

        if (planningTime != null) {
          task.addProjectPlanningTimeListItem(planningTime);
        }
      }

      projectTaskRepo.save(task);
    }
  }
}
