package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class TaskStatusToolServiceImpl implements TaskStatusToolService {

  protected AppProjectService appProjectService;

  @Inject
  public TaskStatusToolServiceImpl(AppProjectService appProjectService) {
    this.appProjectService = appProjectService;
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
  public Set<TaskStatus> getTaskStatusSet(Project project, ProjectTask projectTask) {
    if (project == null
        || projectTask == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return null;
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
        && !ObjectUtils.isEmpty(projectTask.getProjectTaskCategory().getProjectTaskStatusSet())) {
      return projectTask.getProjectTaskCategory().getProjectTaskStatusSet();
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
}
