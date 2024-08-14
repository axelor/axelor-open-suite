package com.axelor.apps.project.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
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
}
