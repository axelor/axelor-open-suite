package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ProjectTaskLinkType;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkTypeService;
import com.axelor.inject.Beans;

public class ProjectTaskLinkTypeManagementRepository extends ProjectTaskLinkTypeRepository {

  @Override
  public void remove(ProjectTaskLinkType projectTaskLinkType) {
    Beans.get(ProjectTaskLinkTypeService.class).emptyOppositeLinkType(projectTaskLinkType);
    super.remove(projectTaskLinkType);
  }
}
