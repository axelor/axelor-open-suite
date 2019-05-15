/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectTemplateServiceImpl implements ProjectTemplateService {

  @Inject private TeamTaskRepository teamTaskRepository;
  @Inject private ProjectManagementRepository projectManagementRepo;

  @Transactional
  @Override
  public Project generateProjectFromTemplate(Project templateProject) {
    ;
    Project projectCopy = copyProject(templateProject);
    if (templateProject.getChildProjectList() != null) {
      for (Project childProject : templateProject.getChildProjectList()) {
        Project childProjectCopy = copyProject(childProject);
        projectCopy.addChildProjectListItem(childProjectCopy);
      }
    }
    return projectManagementRepo.save(projectCopy);
  }

  @Transactional
  public Project copyProject(Project templateProject) {
    Project projectCopy = Beans.get(ProjectManagementRepository.class).copy(templateProject, false);
    projectCopy.setIsTemplate(false);
    projectCopy = projectManagementRepo.save(projectCopy);

    if (templateProject.getTeamTaskList() != null) {
      for (TeamTask task : templateProject.getTeamTaskList()) {
        if (task.getParentTask() == null) {
          copyTeamTask(task, projectCopy);
        }
      }
    }
    return projectCopy;
  }

  @Transactional
  public TeamTask copyTeamTask(TeamTask task, Project projectCopy) {
    TeamTask taskCopy = teamTaskRepository.copy(task, false);

    projectCopy.addTeamTaskListItem(taskCopy);
    taskCopy = teamTaskRepository.save(taskCopy);

    if (task.getTeamTaskList() != null) {

      for (TeamTask childTask : task.getTeamTaskList()) {
        TeamTask childTaskCopy = copyTeamTask(childTask, projectCopy); // Recursive call
        taskCopy.addTeamTaskListItem(childTaskCopy);
      }
    }
    return taskCopy;
  }
}
