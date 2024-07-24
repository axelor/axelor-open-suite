package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;

public interface ProjectTaskLinkService {
  String getLinkTypeDomain(Project project);

  String getProjectTaskDomain(ProjectTask projectTask);
}
