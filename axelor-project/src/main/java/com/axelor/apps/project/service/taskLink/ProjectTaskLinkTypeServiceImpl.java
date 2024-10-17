package com.axelor.apps.project.service.taskLink;

import com.axelor.apps.project.db.ProjectTaskLinkType;
import com.axelor.apps.project.db.repo.ProjectTaskLinkTypeRepository;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectTaskLinkTypeServiceImpl implements ProjectTaskLinkTypeService {

  protected ProjectTaskLinkTypeRepository projectTaskLinkTypeRepository;

  @Inject
  public ProjectTaskLinkTypeServiceImpl(
      ProjectTaskLinkTypeRepository projectTaskLinkTypeRepository) {
    this.projectTaskLinkTypeRepository = projectTaskLinkTypeRepository;
  }

  @Override
  public void generateOppositeLinkType(ProjectTaskLinkType projectTaskLinkType, String name) {
    if (projectTaskLinkType == null || StringUtils.isEmpty(name)) {
      return;
    }

    ProjectTaskLinkType opposite = new ProjectTaskLinkType();
    opposite.setName(name);

    manageOppositeLinkTypes(projectTaskLinkType, opposite);
  }

  @Override
  public void selectOppositeLinkType(
      ProjectTaskLinkType projectTaskLinkType, ProjectTaskLinkType opposite) {
    if (projectTaskLinkType == null || opposite == null) {
      return;
    }

    manageOppositeLinkTypes(projectTaskLinkType, opposite);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void manageOppositeLinkTypes(
      ProjectTaskLinkType projectTaskLinkType, ProjectTaskLinkType opposite) {
    opposite.setOppositeLinkType(projectTaskLinkType);
    opposite = projectTaskLinkTypeRepository.save(opposite);

    emptyOppositeLinkType(projectTaskLinkType);

    projectTaskLinkType.setOppositeLinkType(opposite);
    projectTaskLinkTypeRepository.save(projectTaskLinkType);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void emptyOppositeLinkType(ProjectTaskLinkType projectTaskLinkType) {
    if (projectTaskLinkType == null || projectTaskLinkType.getOppositeLinkType() == null) {
      return;
    }

    ProjectTaskLinkType opposite = projectTaskLinkType.getOppositeLinkType();
    opposite.setOppositeLinkType(null);
    projectTaskLinkTypeRepository.save(opposite);
    projectTaskLinkType.setOppositeLinkType(null);
    projectTaskLinkTypeRepository.save(projectTaskLinkType);
  }
}
