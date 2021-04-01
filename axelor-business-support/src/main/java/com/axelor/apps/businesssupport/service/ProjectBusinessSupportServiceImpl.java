/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businesssupport.service;

import com.axelor.apps.businessproject.service.ProjectBusinessServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.google.inject.Inject;
import java.util.Set;

public class ProjectBusinessSupportServiceImpl extends ProjectBusinessServiceImpl {

  @Inject
  public ProjectBusinessSupportServiceImpl(ProjectRepository projectRepository) {
    super(projectRepository);
  }

  @Override
  public ProjectTask createTask(
      TaskTemplate taskTemplate, Project project, Set<TaskTemplate> taskTemplateSet) {

    ProjectTask task = super.createTask(taskTemplate, project, taskTemplateSet);
    task.setInternalDescription(taskTemplate.getInternalDescription());

    return task;
  }
}
