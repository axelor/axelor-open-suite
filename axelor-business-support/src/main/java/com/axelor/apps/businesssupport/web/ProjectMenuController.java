package com.axelor.apps.businesssupport.web;

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
import java.util.Optional;

public class ProjectMenuController {

  public void myOpenProjectTickets(ActionRequest request, ActionResponse response) {
    Project activeProject =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveProject).orElse(null);

    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Ticket"))
            .model(ProjectTask.class.getName())
            .add("kanban", "project-task-kanban")
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain(
                "self.project.projectStatus.isCompleted = false AND self.typeSelect = :_typeSelect AND (self.project.id IN :_projectIds OR :_project is null) AND :__user__ MEMBER OF self.project.membersUserSet")
            .context("_typeSelect", ProjectTaskRepository.TYPE_TICKET)
            .context("_project", activeProject)
            .context("_projectIds", Beans.get(ProjectToolService.class).getActiveProjectIds())
            .param("search-filters", "project-task-filters")
            .param("forceTitle", "true");

    response.setView(builder.map());
  }
}
