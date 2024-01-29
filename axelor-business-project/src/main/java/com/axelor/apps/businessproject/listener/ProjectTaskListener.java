package com.axelor.apps.businessproject.listener;

import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.PreUpdate;

public class ProjectTaskListener {
  @PreUpdate
  public void onProjectTaskPreUpdate(ProjectTask projectTask) {
    calculateParentTaskProgress(projectTask);
  }

  public void calculateParentTaskProgress(ProjectTask projectTask) {
    ProjectTask parentTask = projectTask.getParentTask();
    if (parentTask != null) {
      List<ProjectTask> childProjectTasks = parentTask.getProjectTaskList();
      childProjectTasks =
          childProjectTasks.stream()
              .map(task -> Objects.equals(task.getId(), projectTask.getId()) ? projectTask : task)
              .collect(Collectors.toList());

      BigDecimal sumProgressTimesPlanifiedTime =
          childProjectTasks.stream()
              .map(
                  task ->
                      BigDecimal.valueOf(task.getProgressSelect()).multiply(task.getPlannedTime()))
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal sumPlannedTime =
          childProjectTasks.stream()
              .map(ProjectTask::getPlannedTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      int scale = 2;
      int averageProgress =
          sumPlannedTime.compareTo(BigDecimal.ZERO) != 0
              ? sumProgressTimesPlanifiedTime
                  .divide(sumPlannedTime, scale, RoundingMode.HALF_UP)
                  .intValue()
              : 0;
      parentTask.setProgressSelect(averageProgress);
    }
  }
}
