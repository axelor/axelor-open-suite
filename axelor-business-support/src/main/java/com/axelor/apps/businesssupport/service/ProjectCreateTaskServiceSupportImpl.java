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
package com.axelor.apps.businesssupport.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.service.ProjectCreateTaskServiceImpl;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.project.service.TaskTemplateService;
import com.google.inject.Inject;
import java.util.Set;

public class ProjectCreateTaskServiceSupportImpl extends ProjectCreateTaskServiceImpl {

  @Inject
  public ProjectCreateTaskServiceSupportImpl(
      ProjectTaskService projectTaskService, TaskTemplateService taskTemplateService) {
    super(projectTaskService, taskTemplateService);
  }

  @Override
  public ProjectTask createTask(
      TaskTemplate taskTemplate, Project project, Set<TaskTemplate> taskTemplateSet)
      throws AxelorException {

    ProjectTask task = super.createTask(taskTemplate, project, taskTemplateSet);
    task.setInternalDescription(taskTemplate.getInternalDescription());

    return task;
  }
}
