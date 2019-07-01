package com.axelor.apps.businesssupport.service;

import com.axelor.apps.businessproject.service.ProjectBusinessServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class ProjectBusinessSupportServiceImpl extends ProjectBusinessServiceImpl {

  @Inject
  public ProjectBusinessSupportServiceImpl(ProjectRepository projectRepository) {
    super(projectRepository);
  }

  @Override
  public TeamTask createTask(TaskTemplate taskTemplate, Project project) {

    TeamTask task = super.createTask(taskTemplate, project);
    task.setInternalDescription(taskTemplate.getInternalDescription());

    return task;
  }
}
