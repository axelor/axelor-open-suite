package com.axelor.apps.project.service;

import com.axelor.apps.project.db.TaskStatus;
import com.axelor.common.ObjectUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectTaskToolServiceImpl implements ProjectTaskToolService {

  public ProjectTaskToolServiceImpl() {}

  @Override
  public TaskStatus getCompletedTaskStatus(
      TaskStatus defaultTaskStatus, Set<TaskStatus> taskStatusSet) {
    TaskStatus completedTaskStatus = null;

    if (!ObjectUtils.isEmpty(taskStatusSet)) {
      completedTaskStatus = defaultTaskStatus;
      if (completedTaskStatus == null || !taskStatusSet.contains(completedTaskStatus)) {
        completedTaskStatus = null;
      }

      if (completedTaskStatus == null) {
        List<TaskStatus> completedTaskStatusList =
            taskStatusSet.stream().filter(TaskStatus::getIsCompleted).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(completedTaskStatusList) && completedTaskStatusList.size() == 1) {
          return completedTaskStatusList.get(0);
        }
      }
    }
    return completedTaskStatus;
  }
}
