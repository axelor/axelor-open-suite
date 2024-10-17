package com.axelor.apps.project.service.taskLink;

import com.axelor.apps.project.db.ProjectTaskLinkType;

public interface ProjectTaskLinkTypeService {

  void generateOppositeLinkType(ProjectTaskLinkType projectTaskLinkType, String name);

  void selectOppositeLinkType(
      ProjectTaskLinkType projectTaskLinkType, ProjectTaskLinkType opposite);

  void emptyOppositeLinkType(ProjectTaskLinkType projectTaskLinkType);
}
