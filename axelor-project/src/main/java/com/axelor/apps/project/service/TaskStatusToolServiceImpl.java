package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.TaskStatusProgressByCategory;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TaskStatusToolServiceImpl implements TaskStatusToolService {

  protected AppProjectService appProjectService;
  protected TaskStatusProgressByCategoryService taskStatusProgressByCategoryService;
  protected TaskStatusRepository taskStatusRepository;

  @Inject
  public TaskStatusToolServiceImpl(
      AppProjectService appProjectService,
      TaskStatusProgressByCategoryService taskStatusProgressByCategoryService,
      TaskStatusRepository taskStatusRepository) {
    this.appProjectService = appProjectService;
    this.taskStatusProgressByCategoryService = taskStatusProgressByCategoryService;
    this.taskStatusRepository = taskStatusRepository;
  }

  @Override
  public Optional<TaskStatus> getCompletedTaskStatus(Project project, ProjectTask projectTask) {
    if (project == null
        || projectTask == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return Optional.empty();
    }

    AppProject appProject = appProjectService.getAppProject();
    boolean enableTaskStatusManagementByCategory = false;
    if (appProject != null) {
      enableTaskStatusManagementByCategory = appProject.getEnableStatusManagementByTaskCategory();
    }

    if (enableTaskStatusManagementByCategory
        && project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY
        && projectTask.getProjectTaskCategory() != null
        && projectTask.getProjectTaskCategory().getCompletedTaskStatus() != null) {
      return Optional.ofNullable(projectTask.getProjectTaskCategory().getCompletedTaskStatus());
    }

    if (Arrays.asList(
                ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT,
                ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY)
            .contains(project.getTaskStatusManagementSelect())
        && project.getCompletedTaskStatus() != null) {
      return Optional.ofNullable(project.getCompletedTaskStatus());
    }

    return Optional.ofNullable(appProject).map(AppProject::getCompletedTaskStatus);
  }

  @Override
  public Set<TaskStatus> getTaskStatusSet(
      Project project,
      ProjectTask projectTask,
      AppProject appProject,
      ProjectTaskCategory category) {
    if (project == null
        || projectTask == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return null;
    }
    if (appProject == null) {
      appProject = appProjectService.getAppProject();
    }
    if (category == null) {
      category = projectTask.getProjectTaskCategory();
    }

    boolean enableTaskStatusManagementByCategory = false;
    if (appProject != null) {
      enableTaskStatusManagementByCategory = appProject.getEnableStatusManagementByTaskCategory();
    }

    if (enableTaskStatusManagementByCategory
        && project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY
        && category != null
        && !ObjectUtils.isEmpty(category.getProjectTaskStatusSet())) {
      return category.getProjectTaskStatusSet();
    }

    if (Arrays.asList(
                ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT,
                ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY)
            .contains(project.getTaskStatusManagementSelect())
        && !ObjectUtils.isEmpty(project.getProjectTaskStatusSet())) {
      return project.getProjectTaskStatusSet();
    }

    if (appProject != null) {
      return appProject.getDefaultTaskStatusSet();
    }
    return null;
  }

  @Override
  public String checkCompletedTaskStatus(Project project, ProjectTask projectTask) {
    if (project == null
        || projectTask == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return "";
    }

    AppProject appProject = appProjectService.getAppProject();
    boolean enableTaskStatusManagementByCategory = false;
    if (appProject != null) {
      enableTaskStatusManagementByCategory = appProject.getEnableStatusManagementByTaskCategory();
    }

    if (enableTaskStatusManagementByCategory
        && project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY
        && projectTask.getProjectTaskCategory() != null
        && projectTask.getProjectTaskCategory().getCompletedTaskStatus() == null) {
      Optional<TaskStatus> taskStatus = getCompletedTaskStatus(project, projectTask);
      if (taskStatus.isPresent()) {
        return String.format(
            I18n.get(
                ProjectExceptionMessage.CATEGORY_COMPLETED_TASK_STATUS_MISSING_WITH_DEFAULT_STATUS),
            I18n.get(taskStatus.get().getName()));
      } else {
        return I18n.get(
            ProjectExceptionMessage.CATEGORY_COMPLETED_TASK_STATUS_MISSING_WITHOUT_DEFAULT_STATUS);
      }
    }

    if (project.getTaskStatusManagementSelect() == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT
        && project.getCompletedTaskStatus() == null) {
      Optional<TaskStatus> taskStatus = getCompletedTaskStatus(project, projectTask);
      if (taskStatus.isPresent()) {
        return String.format(
            I18n.get(
                ProjectExceptionMessage.PROJECT_COMPLETED_TASK_STATUS_MISSING_WITH_DEFAULT_STATUS),
            I18n.get(taskStatus.get().getName()));
      } else {
        return I18n.get(
            ProjectExceptionMessage.PROJECT_COMPLETED_TASK_STATUS_MISSING_WITHOUT_DEFAULT_STATUS);
      }
    }

    return "";
  }

  @Override
  public List<TaskStatusProgressByCategory> getUnmodifiedTaskStatusProgressByCategoryList(
      TaskStatus taskStatus) {
    AppProject appProject = appProjectService.getAppProject();
    if (taskStatus == null
        || taskStatus.getId() == null
        || appProject == null
        || !appProject.getEnableStatusManagementByTaskCategory()
        || !appProject.getSelectAutoProgressOnProjectTask()) {
      return new ArrayList<>();
    }

    taskStatus = taskStatusRepository.find(taskStatus.getId());
    return Query.of(TaskStatusProgressByCategory.class)
        .filter("self.taskStatus.id = :taskStatusId AND self.isCustomized = false")
        .bind("taskStatusId", taskStatus.getId())
        .fetch();
  }

  @Override
  public void updateExistingProgressOnCategory(TaskStatus taskStatus) {
    List<TaskStatusProgressByCategory> taskStatusProgressByCategoryList =
        getUnmodifiedTaskStatusProgressByCategoryList(taskStatus);
    if (!ObjectUtils.isEmpty(taskStatusProgressByCategoryList)) {
      taskStatusProgressByCategoryService.updateExistingProgressWithValue(
          taskStatusProgressByCategoryList, taskStatus.getDefaultProgress());
    }
  }

  @Override
  public List<TaskStatus> getMissingTaskStatus(
      Set<TaskStatus> oldTaskStatusList, Set<TaskStatus> newTaskStatusList) {
    List<TaskStatus> missingTaskStatusList = new ArrayList<>();

    if (ObjectUtils.isEmpty(oldTaskStatusList)) {
      return missingTaskStatusList;
    }

    for (TaskStatus taskStatus : oldTaskStatusList) {
      if (ObjectUtils.isEmpty(newTaskStatusList) || !newTaskStatusList.contains(taskStatus)) {
        missingTaskStatusList.add(taskStatus);
      }
    }
    return missingTaskStatusList;
  }
}
