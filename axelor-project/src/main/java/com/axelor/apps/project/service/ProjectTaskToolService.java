package com.axelor.apps.project.service;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.studio.db.AppProject;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectTaskToolService {
  Optional<TaskStatus> getCompletedTaskStatus(
      TaskStatus defaultTaskStatus, Set<TaskStatus> taskStatusSet);

  List<ProjectTask> getProjectTaskDependingStatus(
      TaskStatus taskStatus, Integer taskStatusManagementSelect, Long objectId);

  List<ProjectTask> getProjectTaskToUpdate(AppProject appProject);

  List<ProjectTask> getProjectTaskListToUpdate(
      Set<TaskStatus> oldTaskStatusSet,
      Set<TaskStatus> newTaskStatusSet,
      Integer taskStatusManagementSelect,
      Long objectId);

  TaskStatus getPreviousTaskStatus(ProjectTask projectTask, AppProject appProject);
}
