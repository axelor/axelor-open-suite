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
package com.axelor.apps.businesssupport.web;

import com.axelor.apps.businesssupport.db.Sprint;
import com.axelor.apps.businesssupport.db.repo.SprintRepository;
import com.axelor.apps.businesssupport.service.sprint.SprintService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            .param("details-view", "true")
            .domain(
                "self.project.projectStatus.isCompleted = false AND self.typeSelect = :_typeSelect AND (self.project.id IN :_projectIds OR :_project is null) AND :__user__ MEMBER OF self.project.membersUserSet")
            .context("_typeSelect", ProjectTaskRepository.TYPE_TICKET)
            .context("_project", activeProject)
            .context("_projectIds", Beans.get(ProjectToolService.class).getActiveProjectIds())
            .param("search-filters", "project-task-filters")
            .param("forceTitle", "true");

    response.setView(builder.map());
  }

  public void viewTasksPerSprint(ActionRequest request, ActionResponse response) {

    Project project = request.getContext().asType(Project.class);

    List<Sprint> sprintList =
        Beans.get(SprintService.class).getCurrentSprintsRelatedToTheProject(project);
    if (!ObjectUtils.isEmpty(sprintList)) {
      List<Long> sprintIdList = sprintList.stream().map(Sprint::getId).collect(Collectors.toList());

      ActionView.ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Tasks per sprint"));
      actionViewBuilder.model(ProjectTask.class.getName());
      actionViewBuilder.add("kanban", "project-task-sprint-kanban");
      actionViewBuilder.add("form", "project-task-form");
      actionViewBuilder.param(
          "kanban-hide-columns",
          Beans.get(SprintRepository.class)
              .all()
              .filter("self.id NOT IN (?1)", sprintIdList)
              .fetchStream()
              .map(sprint -> String.valueOf(sprint.getId()))
              .collect(Collectors.joining(",")));
      actionViewBuilder.domain("self.project.id = :_projectId");
      actionViewBuilder.context("_projectId", project.getId());

      response.setView(actionViewBuilder.map());
    }
  }
}
