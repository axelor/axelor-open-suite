/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectMenuServiceImpl implements ProjectMenuService {

  protected ProjectStatusRepository projectStatusRepo;

  @Inject
  public ProjectMenuServiceImpl(ProjectStatusRepository projectStatusRepo) {
    this.projectStatusRepo = projectStatusRepo;
  }

  @Override
  public Map<String, Object> getAllOpenProjectTasks() {
    ActionViewBuilder builder =
        ActionView.define(I18n.get("Project Tasks"))
            .model(TeamTask.class.getName())
            .add("kanban", "team-task-kanban")
            .add("grid", "team-task-grid")
            .add("form", "team-task-form")
            .domain(
                "self.project.projectStatus.isCompleted = false and self.typeSelect = :_typeSelect")
            .context("_typeSelect", TeamTaskRepository.TYPE_TASK)
            .param(
                "kanban-hide-columns",
                getProjectStatusIds(ProjectStatusRepository.PROJECT_STATUS_PROJECT));

    return builder.map();
  }

  @Override
  public Map<String, Object> getAllOpenProjectTickets() {
    ActionViewBuilder builder =
        ActionView.define(I18n.get("Ticket"))
            .model(TeamTask.class.getName())
            .add("kanban", "team-task-kanban")
            .add("grid", "team-task-grid")
            .add("form", "team-task-form")
            .domain(
                "self.project.projectStatus.isCompleted = false and self.typeSelect = :_typeSelect")
            .context("_typeSelect", TeamTaskRepository.TYPE_TICKET)
            .param("forceTitle", "true")
            .param(
                "kanban-hide-columns",
                getProjectStatusIds(ProjectStatusRepository.PROJECT_STATUS_PROJECT));

    return builder.map();
  }

  @Override
  public Map<String, Object> getAllProjects() {
    ActionViewBuilder builder =
        ActionView.define(I18n.get("Projects"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "project-form")
            .add("kanban", "project-kanban")
            .param(
                "kanban-hide-columns",
                getProjectStatusIds(ProjectStatusRepository.PROJECT_STATUS_TASK));

    return builder.map();
  }

  protected String getProjectStatusIds(int relatedToSelect) {
    return projectStatusRepo
        .all()
        .filter("self.relatedToSelect = ?1", relatedToSelect)
        .fetchStream()
        .map(ProjectStatus::getId)
        .map(String::valueOf)
        .collect(Collectors.joining(","));
  }
}
