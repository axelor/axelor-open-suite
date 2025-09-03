/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.administration.AbstractBatch;
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
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
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
    if (ObjectUtils.isEmpty(getTaskStatusIds())) {
      return;
    }

    removeTaskStatusFromProject();
    removeTaskStatusFromCategory();
    Query<ProjectTask> projectTaskQuery = getProjectTasksToUpdate();

    removeTaskStatus(projectTaskQuery);
  }

  protected Query<ProjectTask> getProjectTasksToUpdate() {
    return projectTaskRepo
        .all()
        .filter(
            "self.status.id IN (:taskStatusIds) AND "
                + "((self.project.taskStatusManagementSelect = :statusProject AND self.project.id IN (:projectIds)) "
                + "OR (self.project.taskStatusManagementSelect = :statusCategory AND self.projectTaskCategory.id IN (:categoryIds)))")
        .bind("taskStatusIds", getTaskStatusIds())
        .bind("statusProject", ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT)
        .bind("statusCategory", ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY)
        .bind("projectIds", getProjectIds())
        .bind("categoryIds", getCategoryIds())
        .order("id");
  }

  protected void removeTaskStatus(Query<ProjectTask> projectTaskQuery) {
    int offset = 0;
    List<ProjectTask> projectTaskList;

    while (!(projectTaskList = projectTaskQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
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
      }
      JPA.clear();
      findBatch();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void removeTaskStatusFromProject() {
    List<Long> projectIds = getProjectIds();
    List<Long> taskStatusIds = getTaskStatusIds();
    if (ObjectUtils.isEmpty(projectIds) || ObjectUtils.isEmpty(taskStatusIds)) {
      return;
    }

    javax.persistence.Query updateQuery =
        JPA.em()
            .createNativeQuery(
                "DELETE FROM project_project_project_task_status_set WHERE project_project IN (:projectIds) AND project_task_status_set IN (:taskStatusIds)")
            .setParameter("projectIds", projectIds)
            .setParameter("taskStatusIds", taskStatusIds);
    updateQuery.executeUpdate();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void removeTaskStatusFromCategory() {
    List<Long> categoryIds = getCategoryIds();
    List<Long> taskStatusIds = getTaskStatusIds();
    if (ObjectUtils.isEmpty(categoryIds) || ObjectUtils.isEmpty(taskStatusIds)) {
      return;
    }

    javax.persistence.Query updateQuery =
        JPA.em()
            .createNativeQuery(
                "DELETE FROM project_project_task_category_project_task_status_set WHERE project_project_task_category IN (:categoryIds) AND project_task_status_set IN (:taskStatusIds)")
            .setParameter("categoryIds", categoryIds)
            .setParameter("taskStatusIds", taskStatusIds);
    updateQuery.executeUpdate();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void resetProjectTaskStatus(ProjectTask projectTask) {
    TaskStatus taskStatus = getPreviousTaskStatus(projectTask);
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

  protected List<Long> getTaskStatusIds() {
    Set<TaskStatus> taskStatusSet =
        Optional.ofNullable(batch)
            .map(Batch::getProjectBatch)
            .map(ProjectBatch::getTaskStatusSet)
            .orElse(new HashSet<>());
    if (ObjectUtils.isEmpty(taskStatusSet)) {
      return new ArrayList<>();
    }
    return taskStatusSet.stream().map(TaskStatus::getId).collect(Collectors.toList());
  }

  protected List<Long> getProjectIds() {
    Set<Project> projectSet =
        Optional.ofNullable(batch)
            .map(Batch::getProjectBatch)
            .map(ProjectBatch::getProjectSet)
            .orElse(new HashSet<>());
    if (ObjectUtils.isEmpty(projectSet)) {
      return new ArrayList<>();
    }
    return projectSet.stream().map(Project::getId).collect(Collectors.toList());
  }

  protected List<Long> getCategoryIds() {
    Set<ProjectTaskCategory> categorySet =
        Optional.ofNullable(batch)
            .map(Batch::getProjectBatch)
            .map(ProjectBatch::getTaskCategorySet)
            .orElse(new HashSet<>());
    if (ObjectUtils.isEmpty(categorySet)) {
      return new ArrayList<>();
    }
    return categorySet.stream().map(ProjectTaskCategory::getId).collect(Collectors.toList());
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
