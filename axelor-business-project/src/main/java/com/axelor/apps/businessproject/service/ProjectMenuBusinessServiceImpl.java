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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectMenuServiceImpl;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import java.util.Map;

public class ProjectMenuBusinessServiceImpl extends ProjectMenuServiceImpl {

  @Inject
  public ProjectMenuBusinessServiceImpl(
      ProjectToolService projectToolService, ProjectTaskRepository projectTaskRepo) {
    super(projectToolService, projectTaskRepo);
  }

  @Override
  public Map<String, Object> getAllProjects(Long projectId) {
    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Projects"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "project-form")
            .add("kanban", "project-kanban")
            .param("search-filters", "project-project-filters")
            .context("_fromBusinessProject", false)
            .domain("self.isBusinessProject = false");

    if (projectId != null) {
      builder.context("_showRecord", projectId);
    }
    return builder.map();
  }

  @Override
  public Map<String, Object> getAllProjectTasks() {
    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Tasks"))
            .model(ProjectTask.class.getName())
            .add("kanban", "project-task-kanban")
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain("self.typeSelect = :_typeSelect AND self.project.isBusinessProject = false")
            .context("_typeSelect", ProjectTaskRepository.TYPE_TASK)
            .param("search-filters", "project-task-filters");

    return builder.map();
  }
}
