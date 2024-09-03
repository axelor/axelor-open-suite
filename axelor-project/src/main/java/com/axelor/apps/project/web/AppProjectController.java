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
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.service.ProjectTaskToolService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.web.tool.ProjectTaskControllerTool;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppProject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Singleton
public class AppProjectController {

  public void generateProjectConfigurations(ActionRequest request, ActionResponse response) {
    Beans.get(AppProjectService.class).generateProjectConfigurations();

    response.setReload(true);
  }

  @ErrorException
  public void manageCompletedTaskStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AppProject appProject = request.getContext().asType(AppProject.class);

    Set<TaskStatus> taskStatusSet = appProject.getDefaultTaskStatusSet();
    Optional<TaskStatus> completedTaskStatus =
        Beans.get(ProjectTaskToolService.class)
            .getCompletedTaskStatus(appProject.getCompletedTaskStatus(), taskStatusSet);

    response.setValue("completedTaskStatus", completedTaskStatus.orElse(null));
  }

  @ErrorException
  public void checkTaskStatusSetChanges(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AppProject appProject = request.getContext().asType(AppProject.class);

    List<ProjectTask> projectTaskList =
        Beans.get(ProjectTaskToolService.class).getProjectTaskToUpdate(appProject);
    ProjectTaskControllerTool.notifyProjectTaskChangeInConfig(
        projectTaskList, appProject, response);
  }

  @ErrorException
  public void updateProjectTaskStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AppProject appProject = request.getContext().asType(AppProject.class);

    List<ProjectTask> projectTaskList =
        Beans.get(ProjectTaskToolService.class).getProjectTaskToUpdate(appProject);
    ProjectTaskControllerTool.updateAllProjectTaskListStatus(projectTaskList, appProject, response);
  }
}
