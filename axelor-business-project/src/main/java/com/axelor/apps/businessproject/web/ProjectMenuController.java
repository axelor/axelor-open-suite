package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
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
}
