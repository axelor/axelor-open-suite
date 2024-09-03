package com.axelor.apps.project.web.tool;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.TaskStatusMassService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppProject;
import java.util.List;

public class ProjectTaskControllerTool {

  public static void notifyProjectTaskChangeInConfig(
      List<ProjectTask> projectTaskList, Model model, ActionResponse response) {
    if (!ObjectUtils.isEmpty(projectTaskList)) {
      if (isNewTaskStatusSetEmpty(model)) {
        response.setError(
            String.format(
                I18n.get(ProjectExceptionMessage.PROJECT_TASK_STATUS_WILL_BE_REMOVED_EMPTY),
                projectTaskList.size()));
      } else {
        response.setAlert(
            String.format(
                I18n.get(ProjectExceptionMessage.PROJECT_TASK_STATUS_WILL_BE_REMOVED),
                projectTaskList.size()));
      }
    }
  }

  public static void updateAllProjectTaskListStatus(
      List<ProjectTask> projectTaskList, AppProject appProject, ActionResponse response) {
    Integer taskUpdated =
        Beans.get(TaskStatusMassService.class)
            .updateTaskStatusOnProjectTask(projectTaskList, appProject);

    if (taskUpdated > 0) {
      response.setNotify(
          String.format(
              I18n.get(ProjectExceptionMessage.PROJECT_TASK_UPDATED_NOTIFY), taskUpdated));
    }
  }

  protected static boolean isNewTaskStatusSetEmpty(Model model) {
    if (model == null) {
      return false;
    }

    if (model instanceof AppProject) {
      return ObjectUtils.isEmpty(((AppProject) model).getDefaultTaskStatusSet());
    } else if (model instanceof Project) {
      return ObjectUtils.isEmpty(((Project) model).getProjectTaskStatusSet());
    } else if (model instanceof ProjectTaskCategory) {
      return ObjectUtils.isEmpty(((ProjectTaskCategory) model).getProjectTaskStatusSet());
    }

    return false;
  }
}
