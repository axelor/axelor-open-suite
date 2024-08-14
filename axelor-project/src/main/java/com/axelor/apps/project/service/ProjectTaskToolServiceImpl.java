package com.axelor.apps.project.service;

import com.axelor.apps.project.db.TaskStatus;
import com.axelor.common.ObjectUtils;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectTaskToolServiceImpl implements ProjectTaskToolService {

  public ProjectTaskToolServiceImpl() {}

  @Override
  public Optional<TaskStatus> getCompletedTaskStatus(
      TaskStatus defaultTaskStatus, Set<TaskStatus> taskStatusSet) {
    Optional<TaskStatus> completedTaskStatus = Optional.empty();

    if (!ObjectUtils.isEmpty(taskStatusSet)) {
      completedTaskStatus = Optional.ofNullable(defaultTaskStatus);
      if (completedTaskStatus.isEmpty() || !taskStatusSet.contains(completedTaskStatus.get())) {
        completedTaskStatus = Optional.empty();
      }

      if (completedTaskStatus.isEmpty()) {
        List<TaskStatus> completedTaskStatusList =
            taskStatusSet.stream().filter(TaskStatus::getIsCompleted).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(completedTaskStatusList) && completedTaskStatusList.size() == 1) {
          return Optional.ofNullable(completedTaskStatusList.get(0));
        }
      }
    }
    return completedTaskStatus;
  }
}
