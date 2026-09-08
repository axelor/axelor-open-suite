/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectTaskProgressUpdateServiceImpl implements ProjectTaskProgressUpdateService {

  public static final int MAX_ITERATIONS = 100;

  protected final ProjectTaskRepository projectTaskRepository;

  @Inject
  public ProjectTaskProgressUpdateServiceImpl(ProjectTaskRepository projectTaskRepository) {
    this.projectTaskRepository = projectTaskRepository;
  }

  @Override
  public ProjectTask updateChildrenProgress(ProjectTask projectTask, BigDecimal progress)
      throws AxelorException {
    Map<Long, List<ProjectTask>> childrenByParentId = fetchSubtreeByParentId(projectTask);
    return updateChildrenProgress(projectTask, progress, 0, childrenByParentId);
  }

  protected ProjectTask updateChildrenProgress(
      ProjectTask projectTask,
      BigDecimal progress,
      int counter,
      Map<Long, List<ProjectTask>> childrenByParentId)
      throws AxelorException {
    checkCounter(counter);
    List<ProjectTask> projectTaskList;
    if (projectTask.getId() == null) {
      projectTaskList = projectTask.getProjectTaskList();
    } else {
      projectTaskList = childrenByParentId.get(projectTask.getId());
    }

    if (projectTaskList != null && !projectTaskList.isEmpty()) {
      for (ProjectTask child : projectTaskList) {
        child.setProgress(progress);
        updateChildrenProgress(child, progress, counter + 1, childrenByParentId);
      }
    }
    return projectTask;
  }

  protected Map<Long, List<ProjectTask>> fetchSubtreeByParentId(ProjectTask root) {
    Map<Long, List<ProjectTask>> childrenByParentId = new HashMap<>();
    if (root.getId() == null) {
      return childrenByParentId;
    }

    List<Long> parentIds = Collections.singletonList(root.getId());
    for (int level = 0; level < MAX_ITERATIONS && !parentIds.isEmpty(); level++) {
      List<ProjectTask> children =
          projectTaskRepository
              .all()
              .filter("self.parentTask.id IN :parentIds")
              .bind("parentIds", parentIds)
              .fetch();

      if (children.isEmpty()) {
        break;
      }

      List<Long> nextParentIds = new ArrayList<>();
      for (ProjectTask child : children) {
        childrenByParentId
            .computeIfAbsent(child.getParentTask().getId(), id -> new ArrayList<>())
            .add(child);
        nextParentIds.add(child.getId());
      }
      parentIds = nextParentIds;
    }
    return childrenByParentId;
  }

  @Override
  public ProjectTask updateParentsProgress(ProjectTask projectTask) throws AxelorException {
    List<Long> ancestorIds = collectAncestorIds(projectTask);
    Map<Long, List<ProjectTask>> siblingsByParentId = fetchChildrenByParentIds(ancestorIds);
    return updateParentsProgress(projectTask, 0, siblingsByParentId);
  }

  protected List<Long> collectAncestorIds(ProjectTask projectTask) throws AxelorException {
    List<Long> ancestorIds = new ArrayList<>();
    ProjectTask current = projectTask;
    for (int counter = 0; current.getParentTask() != null; counter++) {
      checkCounter(counter);
      current = current.getParentTask();
      ancestorIds.add(current.getId());
    }
    return ancestorIds;
  }

  protected Map<Long, List<ProjectTask>> fetchChildrenByParentIds(List<Long> parentIds) {
    if (parentIds.isEmpty()) {
      return Collections.emptyMap();
    }

    return projectTaskRepository
        .all()
        .filter("self.parentTask.id IN :parentIds")
        .bind("parentIds", parentIds)
        .fetch()
        .stream()
        .collect(Collectors.groupingBy(task -> task.getParentTask().getId()));
  }

  protected ProjectTask updateParentsProgress(
      ProjectTask projectTask, int counter, Map<Long, List<ProjectTask>> siblingsByParentId)
      throws AxelorException {
    checkCounter(counter);
    ProjectTask parentTask = projectTask.getParentTask();
    if (parentTask != null) {
      List<ProjectTask> childProjectTasks =
          new ArrayList<>(
              siblingsByParentId.getOrDefault(parentTask.getId(), Collections.emptyList()));
      boolean projectTaskFound = false;
      for (int index = 0; index < childProjectTasks.size(); index++) {
        if (Objects.equals(childProjectTasks.get(index), projectTask)) {
          childProjectTasks.set(index, projectTask);
          projectTaskFound = true;
          break;
        }
      }
      if (!projectTaskFound) {
        childProjectTasks.add(projectTask);
      }

      BigDecimal sumProgressTimesPlanifiedTime =
          childProjectTasks.stream()
              .map(task -> task.getProgress().multiply(task.getBudgetedTime()))
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal sumPlannedTime =
          childProjectTasks.stream()
              .map(ProjectTask::getBudgetedTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal averageProgress = BigDecimal.ZERO;

      if (sumPlannedTime.compareTo(BigDecimal.ZERO) != 0) {
        averageProgress =
            sumProgressTimesPlanifiedTime.divide(
                sumPlannedTime, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
      } else {
        childProjectTasks =
            childProjectTasks.stream()
                .filter(task -> task.getProgress().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(childProjectTasks)) {
          BigDecimal sumProgress =
              childProjectTasks.stream()
                  .map(ProjectTask::getProgress)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          averageProgress =
              sumProgress.divide(
                  BigDecimal.valueOf(childProjectTasks.size()),
                  AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                  RoundingMode.HALF_UP);
        }
      }

      parentTask.setProgress(averageProgress);
      updateParentsProgress(parentTask, counter + 1, siblingsByParentId);
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
