package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.meta.CallMethod;
import java.util.Optional;
import java.util.Set;

public interface ProjectToolService {
  Optional<TaskStatus> getCompletedTaskStatus(Project project);

  Set<TaskStatus> getTaskStatusSet(Project project);

  String checkCompletedTaskStatus(Project project);

  void getChildProjectIds(Set<Long> projectIdsSet, Project project);

  @CallMethod
  Set<Long> getActiveProjectIds();
}
