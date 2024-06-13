package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import java.util.Set;

public interface ProjectCreateTaskService {
  ProjectTask createTask(
      TaskTemplate taskTemplate, Project project, Set<TaskTemplate> taskTemplateSet);
}
