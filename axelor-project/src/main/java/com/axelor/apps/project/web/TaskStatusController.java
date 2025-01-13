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
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.TaskStatusToolService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class TaskStatusController {

  @ErrorException
  public void validateProgressOnCategory(ActionRequest request, ActionResponse response)
      throws AxelorException {
    TaskStatus taskStatus = request.getContext().asType(TaskStatus.class);

    if (!Beans.get(TaskStatusToolService.class)
        .getUnmodifiedTaskStatusProgressByCategoryList(taskStatus)
        .isEmpty()) {
      response.setAlert(
          I18n.get(ProjectExceptionMessage.TASK_STATUS_USED_ON_PROJECT_TASK_CATEGORY));
    }
  }

  @ErrorException
  public void updateExistingProgressOnCategory(ActionRequest request, ActionResponse response)
      throws AxelorException {
    TaskStatus taskStatus = request.getContext().asType(TaskStatus.class);

    Beans.get(TaskStatusToolService.class).updateExistingProgressOnCategory(taskStatus);
  }
}
