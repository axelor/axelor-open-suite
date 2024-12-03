/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.batch.BatchStrategy;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskCategoryRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.TaskStatusToolService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BatchRemoveTaskStatusService extends BatchStrategy {

  protected ProjectTaskRepository projectTaskRepo;
  protected TaskStatusToolService taskStatusToolService;
  protected TaskStatusRepository taskStatusRepository;
  protected ProjectRepository projectRepository;
  protected ProjectTaskCategoryRepository projectTaskCategoryRepository;

  @Inject
  public BatchRemoveTaskStatusService(
      ProjectTaskRepository projectTaskRepo,
      TaskStatusToolService taskStatusToolService,
      TaskStatusRepository taskStatusRepository,
      ProjectRepository projectRepository,
      ProjectTaskCategoryRepository projectTaskCategoryRepository) {
    this.projectTaskRepo = projectTaskRepo;
    this.taskStatusToolService = taskStatusToolService;
    this.taskStatusRepository = taskStatusRepository;
    this.projectRepository = projectRepository;
    this.projectTaskCategoryRepository = projectTaskCategoryRepository;
  }

  @Override
  protected void process() {
    findBatch();
    Set<TaskStatus> taskStatusSet =
        Optional.ofNullable(batch)
            .map(Batch::getProjectBatch)
            .map(ProjectBatch::getTaskStatusSet)
            .orElse(new HashSet<>());
    if (ObjectUtils.isEmpty(taskStatusSet)) {
      return;
    }

    Set<Project> updatedProjectSet = getUpdatedProjectSet(taskStatusSet);
    Set<ProjectTaskCategory> updatedTaskCategorySet = getUpdatedTaskCategorySet(taskStatusSet);
    List<ProjectTask> projectTaskList =
        getProjectTaskListToUpdate(updatedProjectSet, updatedTaskCategorySet, taskStatusSet);

    removeTaskStatus(projectTaskList);
  }

  protected List<ProjectTask> getProjectTaskListToUpdate(
      Set<Project> updatedProjectSet,
      Set<ProjectTaskCategory> updatedTaskCategorySet,
      Set<TaskStatus> taskStatusSet) {
    List<ProjectTask> projectTaskList = new ArrayList<>();
    if (ObjectUtils.isEmpty(taskStatusSet)) {
      return projectTaskList;
    }

    if (!ObjectUtils.isEmpty(updatedProjectSet)) {
      projectTaskList.addAll(
          updatedProjectSet.stream()
              .map(Project::getProjectTaskList)
              .flatMap(Collection::stream)
              .filter(projectTask -> taskStatusSet.contains(projectTask.getStatus()))
              .collect(Collectors.toList()));
    }
    if (!ObjectUtils.isEmpty(updatedTaskCategorySet)) {
      String categoryIdsStr =
          updatedTaskCategorySet.stream()
              .map(ProjectTaskCategory::getId)
              .map(String::valueOf)
              .collect(Collectors.joining(","));
      String statusIdsStr =
          taskStatusSet.stream()
              .map(TaskStatus::getId)
              .map(String::valueOf)
              .collect(Collectors.joining(","));
      projectTaskList.addAll(
          projectTaskRepo
              .all()
              .filter(
                  String.format(
                      "self.projectTaskCategory.id IN (%s) AND self.status.id IN (%s)",
                      categoryIdsStr, statusIdsStr))
              .fetch());
    }

    return projectTaskList;
  }

  protected void removeTaskStatus(List<ProjectTask> projectTaskList) {
    int offset = 0;

    for (ProjectTask projectTask : projectTaskList) {
      offset++;

      try {
        this.resetProjectTaskStatus(projectTask);
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            e,
            String.format(
                I18n.get(ProjectExceptionMessage.BATCH_TASK_STATUS_UPDATE_TASK), projectTask),
            batch.getId());
      }

      if (offset % FETCH_LIMIT == 0) {
        JPA.clear();
        findBatch();
      }
    }
  }

  protected Set<Project> getUpdatedProjectSet(Set<TaskStatus> taskStatusSet) {
    int offset = 0;
    Set<Project> projectSet =
        Optional.ofNullable(batch)
            .map(Batch::getProjectBatch)
            .map(ProjectBatch::getProjectSet)
            .orElse(new HashSet<>());
    Set<Project> updatedProjectSet = new HashSet<>();

    if (ObjectUtils.isEmpty(projectSet) || ObjectUtils.isEmpty(taskStatusSet)) {
      return updatedProjectSet;
    }

    for (Project project : projectSet) {
      offset++;

      try {
        if (isProjectUpdated(project, taskStatusSet)) {
          updatedProjectSet.add(project);
        }
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            e,
            String.format(
                I18n.get(ProjectExceptionMessage.BATCH_TASK_STATUS_UPDATE_PROJECT), project),
            batch.getId());
      }

      if (offset % FETCH_LIMIT == 0) {
        JPA.clear();
        findBatch();
      }
    }

    return updatedProjectSet;
  }

  protected Set<ProjectTaskCategory> getUpdatedTaskCategorySet(Set<TaskStatus> taskStatusSet) {
    int offset = 0;
    Set<ProjectTaskCategory> projectTaskCategorySet =
        Optional.ofNullable(batch)
            .map(Batch::getProjectBatch)
            .map(ProjectBatch::getTaskCategorySet)
            .orElse(new HashSet<>());
    Set<ProjectTaskCategory> updatedProjectTaskCategorySet = new HashSet<>();

    if (ObjectUtils.isEmpty(projectTaskCategorySet) || ObjectUtils.isEmpty(taskStatusSet)) {
      return updatedProjectTaskCategorySet;
    }

    for (ProjectTaskCategory taskCategory : projectTaskCategorySet) {
      offset++;

      try {
        if (isProjectTaskCategoryUpdated(taskCategory, taskStatusSet)) {
          updatedProjectTaskCategorySet.add(taskCategory);
        }
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            e,
            String.format(
                I18n.get(ProjectExceptionMessage.BATCH_TASK_STATUS_UPDATE_PROJECT_TASK_CATEGORY),
                taskCategory),
            batch.getId());
      }

      if (offset % FETCH_LIMIT == 0) {
        JPA.clear();
        findBatch();
      }
    }

    return updatedProjectTaskCategorySet;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected boolean isProjectUpdated(Project project, Set<TaskStatus> taskStatusSet) {
    project = projectRepository.find(project.getId());
    boolean needSave = false;
    if (project.getTaskStatusManagementSelect()
        == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT) {
      for (TaskStatus taskStatus : taskStatusSet) {
        if (project.getProjectTaskStatusSet().contains(taskStatus)) {
          project.removeProjectTaskStatusSetItem(taskStatus);
          if (taskStatus.equals(project.getCompletedTaskStatus())) {
            project.setCompletedTaskStatus(null);
          }
          needSave = true;
        }
      }
    }
    if (needSave) {
      if (ObjectUtils.isEmpty(project.getProjectTaskStatusSet())) {
        project.setTaskStatusManagementSelect(ProjectRepository.TASK_STATUS_MANAGEMENT_NONE);
      }
      projectRepository.save(project);
    }

    return needSave;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected boolean isProjectTaskCategoryUpdated(
      ProjectTaskCategory taskCategory, Set<TaskStatus> taskStatusSet) {
    taskCategory = projectTaskCategoryRepository.find(taskCategory.getId());
    boolean needSave = false;
    for (TaskStatus taskStatus : taskStatusSet) {
      if (taskCategory.getProjectTaskStatusSet().contains(taskStatus)) {
        taskCategory.removeProjectTaskStatusSetItem(taskStatus);
        if (taskStatus.equals(taskCategory.getCompletedTaskStatus())) {
          taskCategory.setCompletedTaskStatus(null);
        }
        needSave = true;
      }
    }
    if (needSave) {
      projectTaskCategoryRepository.save(taskCategory);
    }

    return needSave;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void resetProjectTaskStatus(ProjectTask projectTask) {
    projectTask = projectTaskRepo.find(projectTask.getId());
    TaskStatus taskStatus = getPreviousTaskStatus(projectTask);
    if (taskStatus != null) {
      taskStatus = taskStatusRepository.find(taskStatus.getId());
    }
    projectTask.setStatus(taskStatus);
    projectTask.addBatchSetItem(batch);
    projectTaskRepo.save(projectTask);
  }

  protected TaskStatus getPreviousTaskStatus(ProjectTask projectTask) {
    Project project = Optional.ofNullable(projectTask).map(ProjectTask::getProject).orElse(null);
    Integer taskStatusManagementSelect =
        Optional.ofNullable(project)
            .map(Project::getTaskStatusManagementSelect)
            .orElse(ProjectRepository.TASK_STATUS_MANAGEMENT_NONE);
    TaskStatus previousTaskStatus = null;
    if (taskStatusManagementSelect == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return previousTaskStatus;
    }
    Set<TaskStatus> taskStatusSet = taskStatusToolService.getTaskStatusSet(project, projectTask);
    if (ObjectUtils.isEmpty(taskStatusSet)) {
      return previousTaskStatus;
    }
    if (taskStatusSet.size() == 1) {
      return taskStatusSet.stream().findFirst().orElse(null);
    }

    List<TaskStatus> taskStatusList =
        taskStatusSet.stream()
            .sorted(Comparator.comparing(TaskStatus::getSequence))
            .collect(Collectors.toList());
    for (TaskStatus taskStatus : taskStatusList) {
      if (taskStatus.getSequence() < projectTask.getStatus().getSequence()) {
        previousTaskStatus = taskStatus;
      } else if (previousTaskStatus == null) {
        return taskStatus;
      }
    }
    return previousTaskStatus;
  }

  @Override
  protected void stop() {
    String comment = I18n.get(ProjectExceptionMessage.BATCH_TASK_STATUS_UPDATE_2);
    comment +=
        String.format(
            "\t" + I18n.get(ProjectExceptionMessage.BATCH_TASK_STATUS_UPDATE_DONE),
            batch.getDone());
    comment +=
        String.format("\t" + I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
