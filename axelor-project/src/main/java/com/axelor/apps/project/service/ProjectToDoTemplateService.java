package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectToDoTemplate;
import com.google.inject.persist.Transactional;

public interface ProjectToDoTemplateService {
  @Transactional(rollbackOn = {Exception.class})
  void generateToDoItemsFromTemplate(Project project, ProjectToDoTemplate template);

  void generateToDoItemsFromTemplate(ProjectTask projectTask, ProjectToDoTemplate template);
}
