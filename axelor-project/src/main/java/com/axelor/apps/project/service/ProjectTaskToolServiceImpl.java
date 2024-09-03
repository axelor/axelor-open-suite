package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskCategoryRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.db.AppProject;
import com.axelor.studio.db.repo.AppProjectRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectTaskToolServiceImpl implements ProjectTaskToolService {

  protected ProjectTaskRepository projectTaskRepository;
  protected TaskStatusToolService taskStatusToolService;
  protected AppProjectRepository appProjectRepository;
  protected ProjectTaskCategoryRepository projectTaskCategoryRepository;
  protected TaskStatusRepository taskStatusRepository;
  protected AppProjectService appProjectService;

  @Inject
  public ProjectTaskToolServiceImpl(
      ProjectTaskRepository projectTaskRepository,
      TaskStatusToolService taskStatusToolService,
      AppProjectRepository appProjectRepository,
      ProjectTaskCategoryRepository projectTaskCategoryRepository,
      TaskStatusRepository taskStatusRepository,
      AppProjectService appProjectService) {
    this.projectTaskRepository = projectTaskRepository;
    this.taskStatusToolService = taskStatusToolService;
    this.appProjectRepository = appProjectRepository;
    this.projectTaskCategoryRepository = projectTaskCategoryRepository;
    this.taskStatusRepository = taskStatusRepository;
    this.appProjectService = appProjectService;
  }

  @Override
  public Optional<TaskStatus> getCompletedTaskStatus(
      TaskStatus defaultTaskStatus, Set<TaskStatus> taskStatusSet) {
    Optional<TaskStatus> completedTaskStatus = Optional.empty();

    if (!ObjectUtils.isEmpty(taskStatusSet)) {
      completedTaskStatus = Optional.ofNullable(defaultTaskStatus);
      if (completedTaskStatus.isEmpty() || !taskStatusSet.contains(completedTaskStatus.get())) {
        completedTaskStatus = Optional.empty();
      }

      if (completedTaskStatus.isEmpty()) {
        List<TaskStatus> completedTaskStatusList =
            taskStatusSet.stream().filter(TaskStatus::getIsCompleted).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(completedTaskStatusList) && completedTaskStatusList.size() == 1) {
          return Optional.ofNullable(completedTaskStatusList.get(0));
        }
      }
    }
    return completedTaskStatus;
  }

  @Override
  public List<ProjectTask> getProjectTaskDependingStatus(
      TaskStatus taskStatus, Integer taskStatusManagementSelect, Long objectId) {
    StringBuilder filter = new StringBuilder();
    Map<String, Object> bindings = new HashMap<>();

    filter.append("self.status = :taskStatus ");
    bindings.put("taskStatus", taskStatus);

    if (taskStatusManagementSelect != null) {
      if (ProjectRepository.TASK_STATUS_MANAGEMENT_APP == taskStatusManagementSelect) {
        filter.append(
            "AND (self.project.taskStatusManagementSelect = :taskStatusManagementSelect "
                + "OR (self.project.taskStatusManagementSelect = :categoryTaskStatusManagementSelect AND self.projectTaskCategory IS NULL))");
        bindings.put("taskStatusManagementSelect", taskStatusManagementSelect);
        bindings.put(
            "categoryTaskStatusManagementSelect",
            ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY);
      } else {
        filter.append("AND self.project.taskStatusManagementSelect = :taskStatusManagementSelect ");
        bindings.put("taskStatusManagementSelect", taskStatusManagementSelect);

        if (ProjectRepository.TASK_STATUS_MANAGEMENT_PROJECT == taskStatusManagementSelect) {
          filter.append("AND self.project.id = :id ");
          bindings.put("id", objectId);
        } else if (ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY
            == taskStatusManagementSelect) {
          filter.append("AND self.projectTaskCategory.id = :id ");
          bindings.put("id", objectId);
        }
      }
    }

    return projectTaskRepository.all().filter(filter.toString()).bind(bindings).order("id").fetch();
  }

  @Override
  public List<ProjectTask> getProjectTaskToUpdate(AppProject appProject) {
    if (appProject == null || appProject.getId() == null) {
      return new ArrayList<>();
    }
    AppProject savedAppProject = appProjectRepository.find(appProject.getId());
    return getProjectTaskListToUpdate(
        savedAppProject.getDefaultTaskStatusSet(),
        appProject.getDefaultTaskStatusSet(),
        ProjectRepository.TASK_STATUS_MANAGEMENT_APP,
        null);
  }

  @Override
  public List<ProjectTask> getProjectTaskToUpdate(ProjectTaskCategory category) {
    if (category == null || category.getId() == null) {
      return new ArrayList<>();
    }
    ProjectTaskCategory savedCategory = projectTaskCategoryRepository.find(category.getId());
    return getProjectTaskListToUpdate(
        savedCategory.getProjectTaskStatusSet(),
        category.getProjectTaskStatusSet(),
        ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY,
        category.getId());
  }

  @Override
  public List<ProjectTask> getProjectTaskListToUpdate(
      Set<TaskStatus> oldTaskStatusSet,
      Set<TaskStatus> newTaskStatusSet,
      Integer taskStatusManagementSelect,
      Long objectId) {
    List<ProjectTask> projectTaskList = new ArrayList<>();
    List<TaskStatus> missingTaskStatusList =
        taskStatusToolService.getMissingTaskStatus(oldTaskStatusSet, newTaskStatusSet);

    if (!ObjectUtils.isEmpty(missingTaskStatusList)) {
      for (TaskStatus taskStatus : missingTaskStatusList) {
        projectTaskList.addAll(
            getProjectTaskDependingStatus(taskStatus, taskStatusManagementSelect, objectId));
      }
    }

    return projectTaskList;
  }

  @Override
  public TaskStatus getPreviousTaskStatus(
      ProjectTask projectTask, AppProject appProject, ProjectTaskCategory category) {
    Integer taskStatusManagementSelect =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getTaskStatusManagementSelect)
            .orElse(ProjectRepository.TASK_STATUS_MANAGEMENT_NONE);
    TaskStatus previousTaskStatus = null;
    if (taskStatusManagementSelect == ProjectRepository.TASK_STATUS_MANAGEMENT_NONE) {
      return previousTaskStatus;
    }
    Set<TaskStatus> taskStatusSet =
        taskStatusToolService.getTaskStatusSet(
            projectTask.getProject(), projectTask, appProject, category);
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
