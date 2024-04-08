package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectClosingControlServiceImpl implements ProjectClosingControlService {

  protected ProjectRepository projectRepository;
  protected ProjectStatusRepository projectStatusRepository;

  @Inject
  public ProjectClosingControlServiceImpl(
      ProjectRepository projectRepository, ProjectStatusRepository projectStatusRepository) {
    this.projectRepository = projectRepository;
    this.projectStatusRepository = projectStatusRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String finishProject(Project project) throws AxelorException {
    project.setProjectStatus(projectStatusRepository.getDefaultCompleted());
    projectRepository.save(project);
    return "";
  }
}
