package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectToDoItem;
import com.axelor.apps.project.db.ProjectToDoTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectToDoItemRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;

public class ProjectToDoTemplateServiceImpl implements ProjectToDoTemplateService {

  protected ProjectToDoItemRepository projectToDoItemRepository;
  protected ProjectRepository projectRepository;

  @Inject
  public ProjectToDoTemplateServiceImpl(
      ProjectToDoItemRepository projectToDoItemRepository, ProjectRepository projectRepository) {
    this.projectToDoItemRepository = projectToDoItemRepository;
    this.projectRepository = projectRepository;
  }

  @Override
  public void generateToDoItemsFromTemplate(Project project, ProjectToDoTemplate template) {
    if (project == null
        || template == null
        || ObjectUtils.isEmpty(template.getProjectToDoItemList())) {
      return;
    }

    project.clearProjectToDoItemList();

    for (ProjectToDoItem item : template.getProjectToDoItemList()) {
      ProjectToDoItem copy = projectToDoItemRepository.copy(item, true);
      copy.setProjectToDoTemplate(null);
      project.addProjectToDoItemListItem(copy);
    }
  }

  @Override
  public void generateToDoItemsFromTemplate(ProjectTask projectTask, ProjectToDoTemplate template) {
    if (projectTask == null
        || template == null
        || ObjectUtils.isEmpty(template.getProjectToDoItemList())) {
      return;
    }

    projectTask.clearProjectToDoItemList();

    for (ProjectToDoItem item : template.getProjectToDoItemList()) {
      ProjectToDoItem copy = projectToDoItemRepository.copy(item, true);
      copy.setProjectToDoTemplate(null);
      projectTask.addProjectToDoItemListItem(copy);
    }
  }
}
