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
package com.axelor.apps.project.web;

import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskCategoryRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectDashboardService;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.Optional;

@Singleton
public class ProjectDashboardController {

  public void getData(ActionRequest request, ActionResponse response) {
    Project project = getProject(request);

    if (project == null) {
      return;
    }
    response.setValues(Beans.get(ProjectDashboardService.class).getData(project));
  }

  @ErrorException
  public void showTasksOpenedPerCategory(ActionRequest request, ActionResponse response) {
    showTasks(request, response, "opened");
  }

  @ErrorException
  public void showTasksClosedPerCategory(ActionRequest request, ActionResponse response) {
    showTasks(request, response, "closed");
  }

  @ErrorException
  public void showTasksPerCategory(ActionRequest request, ActionResponse response) {
    showTasks(request, response, "all");
  }

  protected void showTasks(ActionRequest request, ActionResponse response, String status) {
    Context context = request.getContext();
    Project project = getProject(request);

    String domain =
        "self.typeSelect = :typeSelect AND self.project.id IN :projectIds "
            + "AND (self.projectTaskCategory = :taskCategory "
            + "OR (self.projectTaskCategory is null AND :taskCategory is null)) ";

    if (status.equals("opened")) {
      domain += "AND self.status.isCompleted = false ";

    } else if (status.equals("closed")) {
      domain += "AND self.status.isCompleted = true ";
    }
    ProjectTaskCategory projectTaskCategory =
        Beans.get(ProjectTaskCategoryRepository.class)
            .find(Long.valueOf(context.get("categoryId").toString()));

    response.setView(
        ActionView.define(I18n.get("Project Tasks"))
            .model(ProjectTask.class.getName())
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain(domain)
            .context("typeSelect", ProjectTaskRepository.TYPE_TASK)
            .context("_project", project)
            .context(
                "projectIds", Beans.get(ProjectToolService.class).getRelatedProjectIds(project))
            .context("taskCategory", projectTaskCategory)
            .param("search-filters", "project-task-filters")
            .map());
  }

  protected Project getProject(ActionRequest request) {
    Project project = null;
    if (Project.class.equals(request.getContext().getContextClass())) {
      project = request.getContext().asType(Project.class);
    } else {
      Long projectId =
          Optional.ofNullable(request.getContext().get("_id"))
              .map(Object::toString)
              .map(Long::valueOf)
              .orElse(0l);
      project = Beans.get(ProjectRepository.class).find(projectId);
      if (project == null) {
        project = Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveProject).orElse(null);
      }
    }

    return project;
  }
}
