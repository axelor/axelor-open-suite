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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.repo.TimerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskLinkType;
import com.axelor.apps.project.db.repo.ProjectTaskLinkTypeRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.project.service.TimerProjectTaskService;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class ProjectTaskController {

  private static final String HIDDEN_ATTR = "hidden";

  public void manageTimerButtons(ActionRequest request, ActionResponse response) {
    try {
      ProjectTask task = request.getContext().asType(ProjectTask.class);
      TimerProjectTaskService service = Beans.get(TimerProjectTaskService.class);
      if (task.getId() == null) {
        return;
      }
      Timer timer = service.find(task);

      boolean hideStart = false;
      boolean hideCancel = true;
      if (timer != null) {
        hideStart = timer.getStatusSelect() == TimerRepository.TIMER_STARTED;
        hideCancel = timer.getTimerHistoryList().isEmpty();
      }

      response.setAttr("startTimerBtn", HIDDEN_ATTR, hideStart);
      response.setAttr("stopTimerBtn", HIDDEN_ATTR, !hideStart);
      response.setAttr("cancelTimerBtn", HIDDEN_ATTR, hideCancel);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTotalTimerDuration(ActionRequest request, ActionResponse response) {
    try {
      ProjectTask task = request.getContext().asType(ProjectTask.class);
      if (task.getId() == null) {
        return;
      }
      Duration duration = Beans.get(TimerProjectTaskService.class).compute(task);
      response.setValue("$_totalTimerDuration", duration.toMinutes() / 60F);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void startTimer(ActionRequest request, ActionResponse response) {
    try {
      ProjectTask task = request.getContext().asType(ProjectTask.class);
      Beans.get(TimerProjectTaskService.class)
          .start(task, Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void stopTimer(ActionRequest request, ActionResponse response) {
    try {
      ProjectTask task = request.getContext().asType(ProjectTask.class);
      Beans.get(TimerProjectTaskService.class)
          .stop(task, Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelTimer(ActionRequest request, ActionResponse response) {
    try {
      ProjectTask task = request.getContext().asType(ProjectTask.class);
      Beans.get(TimerProjectTaskService.class).cancel(task);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void deleteProjectTask(ActionRequest request, ActionResponse response) {
    try {

      ProjectTask projectTask = request.getContext().asType(ProjectTask.class);
      projectTask = Beans.get(ProjectTaskRepository.class).find(projectTask.getId());
      Beans.get(ProjectTaskService.class).deleteProjectTask(projectTask);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillSubtask(ActionRequest request, ActionResponse response) {
    try {
      ProjectTask projectTask = request.getContext().asType(ProjectTask.class);
      Beans.get(ProjectTaskService.class).fillSubtask(projectTask);
      response.setValues(projectTask);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void setLinkTypeDomain(ActionRequest request, ActionResponse response) {
    Project project =
        Optional.of(request.getContext().asType(ProjectTask.class))
            .map(ProjectTask::getProject)
            .orElse(null);

    String domain = Beans.get(ProjectTaskLinkService.class).getLinkTypeDomain(project);

    response.setAttr("$projectTaskLinkType", "domain", domain);
  }

  @ErrorException
  public void setTaskDomain(ActionRequest request, ActionResponse response) {

    ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

    String domain = Beans.get(ProjectTaskLinkService.class).getProjectTaskDomain(projectTask);

    response.setAttr("$projectTask", "domain", domain);
  }

  @ErrorException
  public void generateNewLink(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ProjectTask projectTask = request.getContext().asType(ProjectTask.class);
    Map<String, Object> relatedTaskMap =
        (Map<String, Object>) request.getContext().get("projectTask");
    Map<String, Object> projectTaskLinkTypeMap =
        (Map<String, Object>) request.getContext().get("projectTaskLinkType");
    if (ObjectUtils.isEmpty(relatedTaskMap) || ObjectUtils.isEmpty(projectTaskLinkTypeMap)) {
      return;
    }

    ProjectTaskRepository projectTaskRepository = Beans.get(ProjectTaskRepository.class);

    projectTask = projectTaskRepository.find(projectTask.getId());
    ProjectTask relatedTask =
        projectTaskRepository.find(Long.valueOf(relatedTaskMap.get("id").toString()));
    ProjectTaskLinkType projectTaskLinkType =
        Beans.get(ProjectTaskLinkTypeRepository.class)
            .find(Long.valueOf(projectTaskLinkTypeMap.get("id").toString()));

    Beans.get(ProjectTaskLinkService.class)
        .generateTaskLink(projectTask, relatedTask, projectTaskLinkType);
    response.setReload(true);
  }
}
