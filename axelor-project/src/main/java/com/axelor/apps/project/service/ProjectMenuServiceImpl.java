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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class ProjectMenuServiceImpl implements ProjectMenuService {

  protected ProjectToolService projectToolService;
  protected ProjectTaskRepository projectTaskRepo;

  @Inject
  public ProjectMenuServiceImpl(
      ProjectToolService projectToolService, ProjectTaskRepository projectTaskRepo) {
    this.projectToolService = projectToolService;
    this.projectTaskRepo = projectTaskRepo;
  }

  @Override
  public Map<String, Object> getAllOpenProjectTasks(Project project) {
    if (project == null) {
      project = Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveProject).orElse(null);
    }

    ActionViewBuilder builder =
        ActionView.define(I18n.get("Project Tasks"))
            .model(ProjectTask.class.getName())
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .add("kanban", "project-task-kanban")
            .domain(
                "self.project.projectStatus.isCompleted = false AND self.typeSelect = :_typeSelect AND (self.project.id IN :_projectIds OR :_project is null) AND :__user__ MEMBER OF self.project.membersUserSet")
            .context("_typeSelect", ProjectTaskRepository.TYPE_TASK)
            .context("_project", project)
            .context("_projectIds", projectToolService.getRelatedProjectIds(project))
            .param("details-view", "true")
            .param("search-filters", "project-task-filters");

    return builder.map();
  }

  @Override
  public Map<String, Object> getAllProjects(Long projectId) {
    ActionViewBuilder builder =
        ActionView.define(I18n.get("Projects"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "project-form")
            .add("kanban", "project-kanban")
            .param("search-filters", "project-project-filters");

    if (projectId != null) {
      builder.context("_showRecord", projectId);
    }

    return builder.map();
  }

  @Override
  public Map<String, Object> getAllProjectTasks() {
    ActionViewBuilder builder =
        ActionView.define(I18n.get("Tasks"))
            .model(ProjectTask.class.getName())
            .add("kanban", "project-task-kanban")
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .param("details-view", "true")
            .domain("self.typeSelect = :_typeSelect")
            .context("_typeSelect", ProjectTaskRepository.TYPE_TASK)
            .param("search-filters", "project-task-filters");

    return builder.map();
  }

  @Override
  public Map<String, Object> getAllProjectRelatedTasks(Project project) {
    ActionViewBuilder builder =
        ActionView.define(I18n.get("Related Tasks"))
            .model(ProjectTask.class.getName())
            .add("tree", "project-project-task-tree")
            .add("kanban", "project-task-kanban")
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .param("details-view", "true")
            .domain("self.typeSelect = :_typeSelect AND self.project.id = :_id")
            .context("_id", project.getId())
            .context("_typeSelect", ProjectTaskRepository.TYPE_TASK)
            .param("search-filters", "project-task-filters");

    return builder.map();
  }
}
