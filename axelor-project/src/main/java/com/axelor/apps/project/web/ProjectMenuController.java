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
package com.axelor.apps.project.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectMenuService;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Optional;

public class ProjectMenuController {

  public void allOpenProjectTasks(ActionRequest request, ActionResponse response) {
    Long projectId =
        Optional.of(request)
            .map(ActionRequest::getContext)
            .map(context -> context.get("projectId"))
            .map(Object::toString)
            .map(Long::valueOf)
            .orElse(null);
    Project project = projectId != null ? Beans.get(ProjectRepository.class).find(projectId) : null;
    response.setView(Beans.get(ProjectMenuService.class).getAllOpenProjectTasks(project));
  }

  public void allProjects(ActionRequest request, ActionResponse response) {
    Long projectId =
        Optional.of(request)
            .map(ActionRequest::getContext)
            .map(context -> context.get("childProjectId"))
            .map(Object::toString)
            .map(Long::valueOf)
            .orElse(null);

    response.setView(Beans.get(ProjectMenuService.class).getAllProjects(projectId));
  }

  public void allProjectTasks(ActionRequest request, ActionResponse response) {
    response.setView(Beans.get(ProjectMenuService.class).getAllProjectTasks());
  }

  public void myProjects(ActionRequest request, ActionResponse response) {
    Project activeProject =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveProject).orElse(null);

    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Project"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "project-form")
            .add("kanban", "project-kanban")
            .domain(
                "(self.id IN :_projectIds OR :_project is null) AND :__user__ MEMBER OF self.membersUserSet")
            .context("_project", activeProject)
            .context("_projectIds", Beans.get(ProjectToolService.class).getActiveProjectIds())
            .param("search-filters", "project-project-filters");

    response.setView(builder.map());
  }

  public void allProjectRelatedTasks(ActionRequest request, ActionResponse response) {

    try {
      Project project =
          Optional.ofNullable(request.getContext())
              .map(Context::getParent)
              .map(c -> c.asType(Project.class))
              .orElse(null);
      if (project == null) {
        return;
      }
      project = Beans.get(ProjectRepository.class).find(project.getId());
      response.setView(Beans.get(ProjectMenuService.class).getAllProjectRelatedTasks(project));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
