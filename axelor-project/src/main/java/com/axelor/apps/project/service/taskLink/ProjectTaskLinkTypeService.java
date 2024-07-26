package com.axelor.apps.project.service.taskLink;

import com.axelor.apps.project.db.ProjectTaskLinkType;

public interface ProjectTaskLinkTypeService {
  void manageOppositeLinkType(
      ProjectTaskLinkType projectTaskLinkType, String name, ProjectTaskLinkType opposite);

  void emptyOppositeLinkType(ProjectTaskLinkType projectTaskLinkType);
}
