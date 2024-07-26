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
  @Transactional(rollbackOn = Exception.class)
  public void manageOppositeLinkType(
      ProjectTaskLinkType projectTaskLinkType, String name, ProjectTaskLinkType opposite) {
    if (projectTaskLinkType == null) {
      return;
    }

    if (!StringUtils.isEmpty(name)) {
      opposite = new ProjectTaskLinkType();
      opposite.setName(name);
    }

    if (opposite != null) {
      opposite.setOppositeLinkType(projectTaskLinkType);
      opposite = projectTaskLinkTypeRepository.save(opposite);

      if (projectTaskLinkType.getOppositeLinkType() != null) {
        ProjectTaskLinkType oldOppositeLinkType = projectTaskLinkType.getOppositeLinkType();
        oldOppositeLinkType.setOppositeLinkType(null);
        projectTaskLinkTypeRepository.save(oldOppositeLinkType);
      }
      projectTaskLinkType.setOppositeLinkType(opposite);
      projectTaskLinkTypeRepository.save(projectTaskLinkType);
    }
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
