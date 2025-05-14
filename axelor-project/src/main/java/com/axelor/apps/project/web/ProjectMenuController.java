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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectMenuService;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.apps.project.service.roadmap.SprintGetService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.ContextHelper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    project = Beans.get(ProjectRepository.class).find(project.getId());
    response.setView(Beans.get(ProjectMenuService.class).getAllProjectRelatedTasks(project));
  }

  public void viewTasksPerSprint(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    SprintGetService sprintGetService = Beans.get(SprintGetService.class);
    List<Sprint> sprintList = sprintGetService.getSprintToDisplayIncludingBacklog(project);

    if (ObjectUtils.notEmpty(sprintList)) {
      String sprintIdsToExclude = sprintGetService.getSprintIdsToExclude(sprintList);

      ActionView.ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Tasks per sprint"));
      actionViewBuilder.model(ProjectTask.class.getName());
      actionViewBuilder.add("kanban", "project-task-sprint-kanban");
      actionViewBuilder.add("form", "project-task-form");
      actionViewBuilder.param("kanban-hide-columns", sprintIdsToExclude);
      actionViewBuilder.domain("self.project.id = :_projectId");
      actionViewBuilder.context("_projectId", project.getId());

      response.setView(actionViewBuilder.map());
    }
  }

  public void viewSprints(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    SprintGetService sprintGetService = Beans.get(SprintGetService.class);
    List<Sprint> sprintList = sprintGetService.getSprintToDisplay(project);
    List<Long> sprintIdList = List.of(0L);
    if (ObjectUtils.notEmpty(sprintList)) {
      sprintIdList = sprintList.stream().map(Sprint::getId).collect(Collectors.toList());
    }

    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Sprints"));
    actionViewBuilder.model(Sprint.class.getName());
    actionViewBuilder.add("grid", "sprint-dashlet-grid");
    actionViewBuilder.add("form", "sprint-form");
    actionViewBuilder.domain("self.id IN (:sprintIds)");
    actionViewBuilder.context("sprintIds", sprintIdList);
    actionViewBuilder.context("sprintManagementSelect", project.getSprintManagementSelect());

    response.setView(actionViewBuilder.map());
  }
}
