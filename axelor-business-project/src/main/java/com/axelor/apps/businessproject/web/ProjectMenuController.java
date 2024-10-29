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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.ContextHelper;
import java.util.Optional;

public class ProjectMenuController {
  public void myBusinessProjects(ActionRequest request, ActionResponse response) {
    Project activeProject =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveProject).orElse(null);

    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Business projects"))
            .model(Project.class.getName())
            .add("grid", "business-project-grid")
            .add("form", "business-project-form")
            .add("kanban", "project-kanban")
            .domain(
                "(self.id IN :_projectIds OR :_project is null) AND :__user__ MEMBER OF self.membersUserSet AND self.isBusinessProject = true")
            .context("_project", activeProject)
            .context("_projectIds", Beans.get(ProjectToolService.class).getActiveProjectIds())
            .context("_fromBusinessProject", true)
            .param("search-filters", "project-project-filters");

    response.setView(builder.map());
  }

  public void myInvoicingProjects(ActionRequest request, ActionResponse response) {
    Project activeProject =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveProject).orElse(null);

    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Invoicing project"))
            .model(InvoicingProject.class.getName())
            .add("grid", "invoicing-project-grid")
            .add("form", "invoicing-project-form")
            .domain(
                "(self.project.id IN :_projectIds OR :_project is null) AND :__user__ MEMBER OF self.project.membersUserSet")
            .context("_project", activeProject)
            .context("_projectIds", Beans.get(ProjectToolService.class).getActiveProjectIds())
            .param("search-filters", "invoicing-project-filters");

    response.setView(builder.map());
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
                "(self.id IN :_projectIds OR :_project is null) AND :__user__ MEMBER OF self.membersUserSet AND self.isBusinessProject = false")
            .context("_project", activeProject)
            .context("_projectIds", Beans.get(ProjectToolService.class).getActiveProjectIds())
            .context("_fromBusinessProject", false)
            .param("search-filters", "project-project-filters");

    response.setView(builder.map());
  }

  @ErrorException
  public void allProjectRelatedTasks(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = null;
    if (Project.class.equals(request.getContext().getContextClass())) {
      project = request.getContext().asType(Project.class);
    } else {
      project = ContextHelper.getContextParent(request.getContext(), Project.class, 1);
    }

    if (project == null) {
      return;
    }

    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Related Tasks"))
            .model(ProjectTask.class.getName())
            .add("grid", "business-project-task-grid")
            .add("kanban", "project-task-kanban")
            .add("form", "business-project-task-form")
            .param("details-view", "true")
            .domain("self.typeSelect = :_typeSelect AND self.project.id = :_id")
            .context("_id", project.getId())
            .context("_typeSelect", ProjectTaskRepository.TYPE_TASK)
            .param("search-filters", "project-task-filters");

    response.setView(builder.map());
  }
}
