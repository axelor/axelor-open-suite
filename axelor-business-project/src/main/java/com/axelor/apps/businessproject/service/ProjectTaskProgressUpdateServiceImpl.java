package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProjectTaskProgressUpdateServiceImpl implements ProjectTaskProgressUpdateService {

  public static final int MAX_ITERATIONS = 100;

  @Override
  public ProjectTask updateChildrenProgress(
      ProjectTask projectTask, BigDecimal progress, int counter) throws AxelorException {
    checkCounter(counter);
    List<ProjectTask> projectTaskList = projectTask.getProjectTaskList();

    if (projectTaskList != null && !projectTaskList.isEmpty()) {
      for (ProjectTask child : projectTaskList) {
        child.setProgress(progress);
        updateChildrenProgress(child, progress, counter + 1);
      }
    }
    return projectTask;
  }

  @Override
  public ProjectTask updateParentsProgress(ProjectTask projectTask, int counter)
      throws AxelorException {
    checkCounter(counter);
    ProjectTask parentTask = projectTask.getParentTask();
    if (parentTask != null) {
      List<ProjectTask> childProjectTasks = parentTask.getProjectTaskList();
      childProjectTasks =
          childProjectTasks.stream()
              .map(task -> Objects.equals(task, projectTask) ? projectTask : task)
              .collect(Collectors.toList());

      BigDecimal sumProgressTimesPlanifiedTime =
          childProjectTasks.stream()
              .map(task -> task.getProgress().multiply(task.getPlannedTime()))
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal sumPlannedTime =
          childProjectTasks.stream()
              .map(ProjectTask::getPlannedTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal averageProgress =
          sumPlannedTime.compareTo(BigDecimal.ZERO) != 0
              ? sumProgressTimesPlanifiedTime.divide(
                  sumPlannedTime, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP)
              : BigDecimal.ZERO;
      parentTask.setProgress(averageProgress);
      updateParentsProgress(parentTask, counter + 1);
    }
    return projectTask;
  }

  protected void checkCounter(int counter) throws AxelorException {
    if (counter >= MAX_ITERATIONS) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProjectExceptionMessage.PROJECT_TASK_INFINITE_LOOP_ISSUE));
    }
  }
}
