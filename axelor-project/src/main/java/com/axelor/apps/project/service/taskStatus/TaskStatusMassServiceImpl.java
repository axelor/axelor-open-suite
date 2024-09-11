package com.axelor.apps.project.service.taskStatus;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.studio.db.AppProject;
import com.axelor.studio.db.repo.AppProjectRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskStatusMassServiceImpl implements TaskStatusMassService {

  protected TaskStatusToolService taskStatusToolService;
  protected ProjectTaskService projectTaskService;
  protected ProjectTaskRepository projectTaskRepository;
  protected TaskStatusRepository taskStatusRepository;
  protected AppProjectRepository appProjectRepository;
  protected final int FETCH_LIMIT = 5;

  @Inject
  public TaskStatusMassServiceImpl(
      TaskStatusToolService taskStatusToolService,
      ProjectTaskService projectTaskService,
      ProjectTaskRepository projectTaskRepository,
      TaskStatusRepository taskStatusRepository,
      AppProjectRepository appProjectRepository) {
    this.taskStatusToolService = taskStatusToolService;
    this.projectTaskService = projectTaskService;
    this.projectTaskRepository = projectTaskRepository;
    this.taskStatusRepository = taskStatusRepository;
    this.appProjectRepository = appProjectRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Integer updateTaskStatusOnProjectTask(
      List<ProjectTask> projectTaskList, AppProject appProject) {
    if (ObjectUtils.isEmpty(projectTaskList)) {
      return 0;
    }

    saveRelatedModels(appProject);

    int i = 0;
    for (ProjectTask projectTask : projectTaskList) {

      this.resetProjectTaskStatus(projectTask);
      i++;

      if (i % FETCH_LIMIT == 0) {
        JPA.clear();
      }
    }

    return i;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void saveRelatedModels(AppProject appProject) {
    if (appProject != null) {
      appProjectRepository.save(EntityHelper.getEntity(appProject));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void resetProjectTaskStatus(ProjectTask projectTask) {
    projectTask = projectTaskRepository.find(projectTask.getId());
    TaskStatus taskStatus = getPreviousTaskStatus(projectTask);
    if (taskStatus != null) {
      taskStatus = taskStatusRepository.find(taskStatus.getId());
      projectTask.setStatus(taskStatus);
      projectTaskService.changeProgress(projectTask, projectTask.getProject());
      projectTaskRepository.save(projectTask);
    }
  }

  protected TaskStatus getPreviousTaskStatus(ProjectTask projectTask) {
    int taskStatusManagementSelect =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getTaskStatusManagementSelect)
            .orElse(ProjectRepository.TASK_STATUS_MANAGEMENT_NONE);
    TaskStatus previousTaskStatus = null;
    if (taskStatusManagementSelect == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return previousTaskStatus;
    }
    Set<TaskStatus> taskStatusSet =
        taskStatusToolService.getTaskStatusSet(projectTask.getProject(), projectTask);
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
}
