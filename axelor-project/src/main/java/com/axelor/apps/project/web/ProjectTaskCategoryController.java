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
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.service.ProjectTaskCategoryService;
import com.axelor.apps.project.service.ProjectTaskToolService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;
import java.util.Set;

public class ProjectTaskCategoryController {

  @ErrorException
  public void manageCompletedTaskStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectTaskCategory projectTaskCategory =
        request.getContext().asType(ProjectTaskCategory.class);

    Set<TaskStatus> taskStatusSet = projectTaskCategory.getProjectTaskStatusSet();
    Optional<TaskStatus> completedTaskStatus =
        Beans.get(ProjectTaskToolService.class)
            .getCompletedTaskStatus(projectTaskCategory.getCompletedTaskStatus(), taskStatusSet);

    response.setValue("completedTaskStatus", completedTaskStatus.orElse(null));
  }

  @ErrorException
  public void fillProgressByCategory(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectTaskCategory projectTaskCategory =
        request.getContext().asType(ProjectTaskCategory.class);

    response.setValue(
        "taskStatusProgressByCategoryList",
        Beans.get(ProjectTaskCategoryService.class).getUpdatedProgressList(projectTaskCategory));
  }

  @ErrorException
  public void validateProgress(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectTaskCategory projectTaskCategory =
        request.getContext().asType(ProjectTaskCategory.class);

    response.setAttr(
        "inconsistencyLabel",
        "hidden",
        Beans.get(ProjectTaskCategoryService.class).verifyProgressValues(projectTaskCategory));
  }
}
