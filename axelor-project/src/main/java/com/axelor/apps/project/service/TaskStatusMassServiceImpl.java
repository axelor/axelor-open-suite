package com.axelor.apps.project.service;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.studio.db.AppProject;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class TaskStatusMassServiceImpl implements TaskStatusMassService {

  protected ProjectTaskToolService projectTaskToolService;
  protected ProjectTaskService projectTaskService;
  protected ProjectTaskRepository projectTaskRepository;
  protected TaskStatusRepository taskStatusRepository;
  protected final int FETCH_LIMIT = 5;

  @Inject
  public TaskStatusMassServiceImpl(
      ProjectTaskToolService projectTaskToolService,
      ProjectTaskService projectTaskService,
      ProjectTaskRepository projectTaskRepository,
      TaskStatusRepository taskStatusRepository) {
    this.projectTaskToolService = projectTaskToolService;
    this.projectTaskService = projectTaskService;
    this.projectTaskRepository = projectTaskRepository;
    this.taskStatusRepository = taskStatusRepository;
  }

  @Override
  public Integer updateTaskStatusOnProjectTask(
      List<ProjectTask> projectTaskList, AppProject appProject) {
    if (ObjectUtils.isEmpty(projectTaskList)) {
      return 0;
    }

    int i = 0;
    for (ProjectTask projectTask : projectTaskList) {

      this.resetProjectTaskStatus(projectTask, appProject);
      i++;

      if (i % FETCH_LIMIT == 0) {
        JPA.clear();
      }
    }

    return i;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void resetProjectTaskStatus(ProjectTask projectTask, AppProject appProject) {
    projectTask = projectTaskRepository.find(projectTask.getId());
    TaskStatus taskStatus = projectTaskToolService.getPreviousTaskStatus(projectTask, appProject);
    if (taskStatus != null) {
      taskStatus = taskStatusRepository.find(taskStatus.getId());
      projectTask.setStatus(taskStatus);
      projectTaskService.changeProgress(projectTask, projectTask.getProject());
      projectTaskRepository.save(projectTask);
    }
  }
}
