package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskStatus;
import java.util.Set;

public interface ProjectToolService {
  TaskStatus getCompletedTaskStatus(Project project);

  Set<TaskStatus> getTaskStatusSet(Project project);

  String checkCompletedTaskStatus(Project project);
}
