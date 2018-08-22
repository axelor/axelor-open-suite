/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.project;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class ProjectServiceImpl implements ProjectService {

  public static final int MAX_LEVEL_OF_PROJECT = 10;

  private ProjectPlanningTimeRepository projectPlanningRepo;
  private ProjectRepository projectRepository;

  @Inject
  public ProjectServiceImpl(
      ProjectPlanningTimeRepository projectPlanningRepo, ProjectRepository projectRepository) {
    this.projectPlanningRepo = projectPlanningRepo;
    this.projectRepository = projectRepository;
  }

  @Override
  @Transactional
  public List<ProjectPlanningTime> createPlanning(Project project) {
    project = projectRepository.find(project.getId());

    List<ProjectPlanningTime> plannings = new ArrayList<>();

    if (project.getExcludePlanning()) {
      return plannings;
    }

    if (project.getAssignedTo() != null) {
      ProjectPlanningTime projectPlanning =
          projectPlanningRepo
              .all()
              .filter("self.project = ?1 and self.task is null", project)
              .fetchOne();
      if (projectPlanning == null) {
        projectPlanning = new ProjectPlanningTime();
        projectPlanning.setProject(project);
        projectPlanning.setUser(project.getAssignedTo());
      }
      plannings.add(projectPlanningRepo.save(projectPlanning));
    }

    for (TeamTask task : project.getTeamTaskList()) {
      ProjectPlanningTime taskPlanning = createPlanning(project, task);
      if (taskPlanning != null) {
        plannings.add(taskPlanning);
      }
    }

    for (Project child : project.getChildProjectList()) {
      plannings.addAll(createPlanning(child));
    }

    return plannings;
  }

  @Override
  @Transactional
  public ProjectPlanningTime createPlanning(Project project, TeamTask task) {

    if (task.getAssignedTo() != null) {
      ProjectPlanningTime projectPlanning =
          projectPlanningRepo
              .all()
              .filter("self.project = ?1 and self.task = ?2", project, task)
              .fetchOne();
      if (projectPlanning == null) {
        projectPlanning = new ProjectPlanningTime();
        projectPlanning.setProject(project);
        projectPlanning.setUser(task.getAssignedTo());
        projectPlanning.setTask(task);
        projectPlanning = projectPlanningRepo.save(projectPlanning);
      }
      return projectPlanning;
    }

    return null;
  }
}
