package com.axelor.apps.project.service;

import com.axelor.apps.project.db.TaskStatus;
import java.util.Optional;
import java.util.Set;

public interface ProjectTaskToolService {
  Optional<TaskStatus> getCompletedTaskStatus(
      TaskStatus defaultTaskStatus, Set<TaskStatus> taskStatusSet);
}
