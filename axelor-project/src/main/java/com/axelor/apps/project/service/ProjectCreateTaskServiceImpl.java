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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.Set;

public class ProjectCreateTaskServiceImpl implements ProjectCreateTaskService {

  ProjectTaskService projectTaskService;

  @Inject
  public ProjectCreateTaskServiceImpl(ProjectTaskService projectTaskService) {
    this.projectTaskService = projectTaskService;
  }

  public ProjectTask createTask(
      TaskTemplate taskTemplate, Project project, Set<TaskTemplate> taskTemplateSet) {

    if (!ObjectUtils.isEmpty(project.getProjectTaskList())) {
      for (ProjectTask projectTask : project.getProjectTaskList()) {
        if (projectTask.getName().equals(taskTemplate.getName())) {
          return projectTask;
        }
      }
    }
    ProjectTask task =
        projectTaskService.create(taskTemplate.getName(), project, taskTemplate.getAssignedTo());
    task.setDescription(taskTemplate.getDescription());
    ProjectTaskCategory projectTaskCategory = taskTemplate.getProjectTaskCategory();
    if (projectTaskCategory != null) {
      task.setProjectTaskCategory(projectTaskCategory);
      project.addProjectTaskCategorySetItem(projectTaskCategory);
    }

    TaskTemplate parentTaskTemplate = taskTemplate.getParentTaskTemplate();

    if (parentTaskTemplate != null && taskTemplateSet.contains(parentTaskTemplate)) {
      task.setParentTask(this.createTask(parentTaskTemplate, project, taskTemplateSet));
      return task;
    }
    return task;
  }
}
