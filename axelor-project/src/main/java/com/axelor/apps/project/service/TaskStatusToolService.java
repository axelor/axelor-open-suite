package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.TaskStatusProgressByCategory;
import com.axelor.studio.db.AppProject;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TaskStatusToolService {
  Optional<TaskStatus> getCompletedTaskStatus(Project project, ProjectTask projectTask);

  Set<TaskStatus> getTaskStatusSet(
      Project project,
      ProjectTask projectTask,
      AppProject appProject,
      ProjectTaskCategory category);

  String checkCompletedTaskStatus(Project project, ProjectTask projectTask);

  List<TaskStatusProgressByCategory> getUnmodifiedTaskStatusProgressByCategoryList(
      TaskStatus taskStatus);

  void updateExistingProgressOnCategory(TaskStatus taskStatus);

  List<TaskStatus> getMissingTaskStatus(
      Set<TaskStatus> oldTaskStatusList, Set<TaskStatus> newTaskStatusList);
}
