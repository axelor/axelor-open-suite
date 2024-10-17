package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.db.ProjectCheckListTemplate;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectCheckListItemRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;

public class ProjectCheckListTemplateServiceImpl implements ProjectCheckListTemplateService {

  protected ProjectCheckListItemRepository projectCheckListItemRepository;
  protected ProjectRepository projectRepository;

  @Inject
  public ProjectCheckListTemplateServiceImpl(
      ProjectCheckListItemRepository projectCheckListItemRepository,
      ProjectRepository projectRepository) {
    this.projectCheckListItemRepository = projectCheckListItemRepository;
    this.projectRepository = projectRepository;
  }

  @Override
  public void generateCheckListItemsFromTemplate(
      Project project, ProjectCheckListTemplate template) {
    if (project == null
        || template == null
        || ObjectUtils.isEmpty(template.getProjectCheckListItemList())) {
      return;
    }

    project.clearProjectCheckListItemList();

    for (ProjectCheckListItem item : template.getProjectCheckListItemList()) {
      ProjectCheckListItem copy = projectCheckListItemRepository.copy(item, true);
      copy.setProjectCheckListTemplate(null);
      project.addProjectCheckListItemListItem(copy);
    }
  }

  @Override
  public void generateCheckListItemsFromTemplate(
      ProjectTask projectTask, ProjectCheckListTemplate template) {
    if (projectTask == null
        || template == null
        || ObjectUtils.isEmpty(template.getProjectCheckListItemList())) {
      return;
    }

    projectTask.clearProjectCheckListItemList();

    for (ProjectCheckListItem item : template.getProjectCheckListItemList()) {
      ProjectCheckListItem copy = projectCheckListItemRepository.copy(item, true);
      copy.setProjectCheckListTemplate(null);
      projectTask.addProjectCheckListItemListItem(copy);
    }
  }
}
