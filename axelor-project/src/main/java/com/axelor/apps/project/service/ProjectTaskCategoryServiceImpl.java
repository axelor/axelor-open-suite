package com.axelor.apps.project.service;

import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.TaskStatusProgressByCategory;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectTaskCategoryServiceImpl implements ProjectTaskCategoryService {

  @Inject
  public ProjectTaskCategoryServiceImpl() {}

  @Override
  public List<TaskStatusProgressByCategory> getUpdatedProgressList(
      ProjectTaskCategory projectTaskCategory) {
    List<TaskStatusProgressByCategory> taskStatusProgressByCategoryList = new ArrayList<>();
    if (projectTaskCategory == null
        || ObjectUtils.isEmpty(projectTaskCategory.getProjectTaskStatusSet())) {
      return taskStatusProgressByCategoryList;
    }

    if (projectTaskCategory.getTaskStatusProgressByCategoryList() == null) {
      projectTaskCategory.setTaskStatusProgressByCategoryList(new ArrayList<>());
    }

    for (TaskStatus taskStatus :
        projectTaskCategory.getProjectTaskStatusSet().stream()
            .sorted(Comparator.comparing(TaskStatus::getSequence))
            .collect(Collectors.toList())) {
      TaskStatusProgressByCategory taskStatusProgressByCategory =
          projectTaskCategory.getTaskStatusProgressByCategoryList().stream()
              .filter(progress -> Objects.equals(progress.getTaskStatus(), taskStatus))
              .findAny()
              .orElse(null);
      if (taskStatusProgressByCategory != null) {
        taskStatusProgressByCategoryList.add(taskStatusProgressByCategory);
      } else {
        taskStatusProgressByCategory = new TaskStatusProgressByCategory();
        taskStatusProgressByCategory.setTaskStatus(taskStatus);
        taskStatusProgressByCategory.setProgress(taskStatus.getDefaultProgress());
        taskStatusProgressByCategoryList.add(taskStatusProgressByCategory);
      }
    }

    return taskStatusProgressByCategoryList;
  }

  @Override
  public boolean verifyProgressValues(ProjectTaskCategory projectTaskCategory) {
    if (projectTaskCategory == null
        || ObjectUtils.isEmpty(projectTaskCategory.getTaskStatusProgressByCategoryList())) {
      return true;
    }
    BigDecimal oldValue = BigDecimal.ZERO;
    for (TaskStatusProgressByCategory taskStatusProgressByCategory :
        projectTaskCategory.getTaskStatusProgressByCategoryList().stream()
            .filter(
                progress ->
                    !Optional.ofNullable(progress)
                        .map(TaskStatusProgressByCategory::getTaskStatus)
                        .map(TaskStatus::getIsCompleted)
                        .orElse(true))
            .sorted(Comparator.comparing(progress -> progress.getTaskStatus().getSequence()))
            .collect(Collectors.toList())) {
      if (taskStatusProgressByCategory.getProgress().compareTo(oldValue) < 0) {
        return false;
      } else {
        oldValue = taskStatusProgressByCategory.getProgress();
      }
    }
    return true;
  }
}
