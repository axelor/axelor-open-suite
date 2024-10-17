package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectCheckListTemplate;
import com.axelor.apps.project.db.ProjectTask;

public interface ProjectCheckListTemplateService {
  void generateCheckListItemsFromTemplate(Project project, ProjectCheckListTemplate template);

  void generateCheckListItemsFromTemplate(
      ProjectTask projectTask, ProjectCheckListTemplate template);
}
