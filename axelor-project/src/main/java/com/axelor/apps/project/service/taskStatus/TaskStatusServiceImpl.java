package com.axelor.apps.project.service.taskStatus;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.db.AppProject;
import com.axelor.studio.db.repo.AppProjectRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskStatusServiceImpl implements TaskStatusService {

  protected AppProjectRepository appProjectRepository;
  protected ProjectTaskRepository projectTaskRepository;

  @Inject
  public TaskStatusServiceImpl(
      AppProjectRepository appProjectRepository, ProjectTaskRepository projectTaskRepository) {
    this.appProjectRepository = appProjectRepository;
    this.projectTaskRepository = projectTaskRepository;
  }

  protected List<TaskStatus> getMissingTaskStatus(
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

  @Override
  public List<ProjectTask> getProjectTaskToUpdate(AppProject appProject) {
    if (appProject == null || appProject.getId() == null) {
      return new ArrayList<>();
    }
    AppProject savedAppProject = appProjectRepository.find(appProject.getId());
    return getProjectTaskListToUpdate(
        savedAppProject.getDefaultTaskStatusSet(), appProject.getDefaultTaskStatusSet());
  }

  protected List<ProjectTask> getProjectTaskListToUpdate(
      Set<TaskStatus> oldTaskStatusSet, Set<TaskStatus> newTaskStatusSet) {
    List<ProjectTask> projectTaskList = new ArrayList<>();
    List<TaskStatus> missingTaskStatusList =
        getMissingTaskStatus(oldTaskStatusSet, newTaskStatusSet);

    if (!ObjectUtils.isEmpty(missingTaskStatusList)) {
      for (TaskStatus taskStatus : missingTaskStatusList) {
        projectTaskList.addAll(getProjectTaskDependingStatus(taskStatus));
      }
    }

    return projectTaskList;
  }

  protected List<ProjectTask> getProjectTaskDependingStatus(TaskStatus taskStatus) {
    StringBuilder filter = new StringBuilder();
    Map<String, Object> bindings = new HashMap<>();

    filter.append("self.status = :taskStatus ");
    bindings.put("taskStatus", taskStatus);
    filter.append(
        "AND (self.project.taskStatusManagementSelect = :taskStatusManagementSelect "
            + "OR (self.project.taskStatusManagementSelect = :categoryTaskStatusManagementSelect AND self.projectTaskCategory IS NULL))");
    bindings.put("taskStatusManagementSelect", ProjectRepository.TASK_STATUS_MANAGEMENT_APP);
    bindings.put(
        "categoryTaskStatusManagementSelect", ProjectRepository.TASK_STATUS_MANAGEMENT_CATEGORY);

    return projectTaskRepository.all().filter(filter.toString()).bind(bindings).order("id").fetch();
  }
}
