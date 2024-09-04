package com.axelor.apps.project.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.taskStatus.TaskStatusToolService;
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
