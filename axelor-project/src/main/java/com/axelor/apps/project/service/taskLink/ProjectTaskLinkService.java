package com.axelor.apps.project.service.taskLink;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskLink;
import com.axelor.apps.project.db.ProjectTaskLinkType;

public interface ProjectTaskLinkService {
  String getLinkTypeDomain(Project project);

  String getProjectTaskDomain(ProjectTask projectTask);

  void removeLink(ProjectTaskLink projectTaskLink);

  void generateTaskLink(
      ProjectTask projectTask, ProjectTask relatedTask, ProjectTaskLinkType projectTaskLinkType);
}
