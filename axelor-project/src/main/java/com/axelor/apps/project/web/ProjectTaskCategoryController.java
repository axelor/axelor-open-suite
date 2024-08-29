package com.axelor.apps.project.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.service.ProjectTaskCategoryService;
import com.axelor.apps.project.service.ProjectTaskToolService;
import com.axelor.apps.project.web.tool.ProjectTaskControllerTool;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
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

  @ErrorException
  public void checkTaskStatusSetChanges(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectTaskCategory projectTaskCategory =
        request.getContext().asType(ProjectTaskCategory.class);

    List<ProjectTask> projectTaskList =
        Beans.get(ProjectTaskToolService.class).getProjectTaskToUpdate(projectTaskCategory);
    ProjectTaskControllerTool.notifyProjectTaskChangeInConfig(
        projectTaskList, projectTaskCategory, response);
  }

  @ErrorException
  public void updateProjectTaskStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectTaskCategory projectTaskCategory =
        request.getContext().asType(ProjectTaskCategory.class);

    List<ProjectTask> projectTaskList =
        Beans.get(ProjectTaskToolService.class).getProjectTaskToUpdate(projectTaskCategory);
    ProjectTaskControllerTool.updateAllProjectTaskListStatus(
        projectTaskList, null, null, projectTaskCategory, response);
  }
}
