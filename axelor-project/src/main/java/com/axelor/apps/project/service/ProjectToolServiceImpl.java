package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ProjectToolServiceImpl implements ProjectToolService {

  protected AppProjectService appProjectService;

  @Inject
  public ProjectToolServiceImpl(AppProjectService appProjectService) {
    this.appProjectService = appProjectService;
  }

  @Override
  public Optional<TaskStatus> getCompletedTaskStatus(Project project) {
    if (project == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return Optional.empty();
    }

    if (project.getTaskStatusManagementSelect() == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT
        && project.getCompletedTaskStatus() != null) {
      return Optional.ofNullable(project.getCompletedTaskStatus());
    }

    return Optional.ofNullable(appProjectService.getAppProject())
        .map(AppProject::getCompletedTaskStatus);
  }

  @Override
  public Set<TaskStatus> getTaskStatusSet(Project project) {
    if (project == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return null;
    }

    if (project.getTaskStatusManagementSelect() == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT
        && !ObjectUtils.isEmpty(project.getProjectTaskStatusSet())) {
      return project.getProjectTaskStatusSet();
    }
    AppProject appProject = appProjectService.getAppProject();
    if (appProject != null) {
      return appProject.getDefaultTaskStatusSet();
    }
    return null;
  }

  @Override
  public String checkCompletedTaskStatus(Project project) {
    if (project == null
        || project.getTaskStatusManagementSelect()
            == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return "";
    }

    if (project.getTaskStatusManagementSelect() == ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT
        && project.getCompletedTaskStatus() == null) {
      Optional<TaskStatus> taskStatus = getCompletedTaskStatus(project);
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
  public void getChildProjectIds(Set<Long> projectIdsSet, Project project) {
    if (projectIdsSet.contains(project.getId())) {
      return;
    }

    projectIdsSet.add(project.getId());

    for (Project childProject : project.getChildProjectList()) {
      getChildProjectIds(projectIdsSet, childProject);
    }
  }

  @Override
  public Set<Long> getActiveProjectIds() {
    User currentUser = AuthUtils.getUser();
    Project activateProject = currentUser.getActiveProject();
    Set<Long> projectIdsSet = new HashSet<>();
    if (activateProject == null) {
      projectIdsSet.add(0l);
      return projectIdsSet;
    }
    if (!currentUser.getIsIncludeSubContextProjects()) {
      projectIdsSet.add(activateProject.getId());
      return projectIdsSet;
    }
    this.getChildProjectIds(projectIdsSet, activateProject);
    return projectIdsSet;
  }
}
