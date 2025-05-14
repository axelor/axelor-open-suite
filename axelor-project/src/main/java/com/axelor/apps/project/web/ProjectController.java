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
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.ProjectCheckListTemplate;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectBatchRepository;
import com.axelor.apps.project.db.repo.ProjectCheckListTemplateRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.ProjectCheckListTemplateService;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.project.service.ProjectTaskToolService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.service.batch.ProjectBatchInitService;
import com.axelor.apps.project.service.roadmap.SprintService;
import com.axelor.apps.project.web.tool.ProjectBatchControllerTool;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppProject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ProjectController {

  public void importMembers(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getTeam() != null) {
      project.getTeam().getMembers().forEach(project::addMembersUserSetItem);
      response.setValue("membersUserSet", project.getMembersUserSet());
    }
  }

  public void getMyOpenTasks(ActionRequest request, ActionResponse response) {
    Project project =
        Beans.get(ProjectRepository.class).find(request.getContext().asType(Project.class).getId());
    Map<String, Object> context = getTaskContext(project);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                project,
                "My open tasks",
                "self.assignedTo = :__user__ AND self.status.isCompleted = false AND self.typeSelect = :_typeSelect AND self.project = :_project",
                context);
    response.setView(view);
  }

  public void getMyTasks(ActionRequest request, ActionResponse response) {
    Project project =
        Beans.get(ProjectRepository.class).find(request.getContext().asType(Project.class).getId());
    Map<String, Object> context = getTaskContext(project);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                project,
                "My tasks",
                "self.createdBy = :__user__ AND self.typeSelect = :_typeSelect AND self.project = :_project",
                context);
    response.setView(view);
  }

  public void getAllOpenTasks(ActionRequest request, ActionResponse response) {
    Project project =
        Beans.get(ProjectRepository.class).find(request.getContext().asType(Project.class).getId());
    Map<String, Object> context = getTaskContext(project);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                project,
                "All open tasks",
                "self.status.isCompleted = false AND self.typeSelect = :_typeSelect AND self.project = :_project",
                context);
    response.setView(view);
  }

  public void getAllTasks(ActionRequest request, ActionResponse response) {
    Project project =
        Beans.get(ProjectRepository.class).find(request.getContext().asType(Project.class).getId());
    Map<String, Object> context = getTaskContext(project);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                project,
                "All tasks",
                "self.typeSelect = :_typeSelect AND self.project = :_project",
                context);
    response.setView(view);
  }

  public void perStatusKanban(ActionRequest request, ActionResponse response) {
    Project project =
        Beans.get(ProjectRepository.class).find(request.getContext().asType(Project.class).getId());
    Map<String, Object> context = getTaskContext(project);
    Map<String, Object> view = Beans.get(ProjectService.class).getPerStatusKanban(project, context);
    response.setView(view);
  }

  protected Map<String, Object> getTaskContext(Project project) {
    Map<String, Object> context = new HashMap<>();
    context.put("_project", project);
    context.put("_typeSelect", ProjectTaskRepository.TYPE_TASK);
    return context;
  }

  public void checkIfResourceBooked(ActionRequest request, ActionResponse response) {
    if (Beans.get(AppProjectService.class).getAppProject().getCheckResourceAvailibility()) {
      Project project = request.getContext().asType(Project.class);
      if (Beans.get(ProjectService.class).checkIfResourceBooked(project)) {
        response.setError(I18n.get(ProjectExceptionMessage.RESOURCE_ALREADY_BOOKED_ERROR_MSG));
      }
    }
  }

  @ErrorException
  public void manageCompletedTaskStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);

    Set<TaskStatus> taskStatusSet = project.getProjectTaskStatusSet();
    Optional<TaskStatus> completedTaskStatus =
        Beans.get(ProjectTaskToolService.class)
            .getCompletedTaskStatus(project.getCompletedTaskStatus(), taskStatusSet);

    response.setValue("completedTaskStatus", completedTaskStatus.orElse(null));
  }

  @ErrorException
  public void taskStatusManagementSelectionIn(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AppProject appProject = Beans.get(AppProjectService.class).getAppProject();

    List<Integer> taskStatusSelect = new ArrayList<>();
    taskStatusSelect.addAll(
        Arrays.asList(
            ProjectRepository.TASK_STATUS_MANAGEMENT_NONE,
            ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT));
    if (appProject != null && appProject.getEnableStatusManagementByTaskCategory()) {
      taskStatusSelect.add(ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY);
    }

    response.setAttr("taskStatusManagementSelect", "selection-in", taskStatusSelect);
  }

  @ErrorException
  public void generateCheckListFromTemplate(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);

    Map<String, Object> checkListTemplateMap =
        (Map<String, Object>) request.getContext().get("projectCheckListTemplate");
    if (ObjectUtils.isEmpty(checkListTemplateMap)) {
      return;
    }

    ProjectCheckListTemplate template =
        Beans.get(ProjectCheckListTemplateRepository.class)
            .find(Long.valueOf(checkListTemplateMap.get("id").toString()));

    Beans.get(ProjectCheckListTemplateService.class)
        .generateCheckListItemsFromTemplate(project, template);
    response.setValue("projectCheckListItemList", project.getProjectCheckListItemList());
  }

  public void generateBacklogSprint(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Beans.get(SprintService.class).generateBacklogSprint(project);
    response.setValue("backlogSprint", project.getBacklogSprint());
  }

  public void removeTaskStatus(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    List<LinkedHashMap<String, Object>> statusToRemoveList =
        (List<LinkedHashMap<String, Object>>) request.getContext().get("statusToRemoveSet");

    if (project != null && ObjectUtils.notEmpty(statusToRemoveList)) {
      TaskStatusRepository taskStatusRepository = Beans.get(TaskStatusRepository.class);
      Set<TaskStatus> taskStatusSet =
          statusToRemoveList.stream()
              .map(it -> taskStatusRepository.find(Long.valueOf(it.get("id").toString())))
              .collect(Collectors.toSet());
      Set<Project> projectSet = new HashSet<>();
      projectSet.add(Beans.get(ProjectRepository.class).find(project.getId()));

      ProjectBatch projectBatch =
          Beans.get(ProjectBatchInitService.class)
              .initializeProjectBatchWithProjects(
                  ProjectBatchRepository.ACTION_REMOVE_TASK_STATUS, projectSet, taskStatusSet);
      ProjectBatchControllerTool.runBatch(projectBatch, response);
    }
  }

  public void checkSprintOverlap(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (Beans.get(SprintService.class).checkSprintOverlap(project)) {
      response.setError(ProjectExceptionMessage.PROJECT_SPRINTS_OVERLAPPED);
    }
  }
}
