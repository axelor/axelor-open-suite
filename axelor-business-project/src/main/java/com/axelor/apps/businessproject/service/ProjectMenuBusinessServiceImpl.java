package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectMenuServiceImpl;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import java.util.Map;

public class ProjectMenuBusinessServiceImpl extends ProjectMenuServiceImpl {

  @Inject
  public ProjectMenuBusinessServiceImpl(
      ProjectToolService projectToolService, ProjectTaskRepository projectTaskRepo) {
    super(projectToolService, projectTaskRepo);
  }

  @Override
  public Map<String, Object> getAllProjects(Long projectId) {
    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Projects"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "project-form")
            .add("kanban", "project-kanban")
            .param("search-filters", "project-project-filters")
            .context("_fromBusinessProject", false)
            .domain("self.isBusinessProject = false");

    if (projectId != null) {
      builder.context("_showRecord", projectId);
    }
    return builder.map();
  }

  @Override
  public Map<String, Object> getAllProjectTasks() {
    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Tasks"))
            .model(ProjectTask.class.getName())
            .add("kanban", "project-task-kanban")
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain("self.typeSelect = :_typeSelect AND self.project.isBusinessProject = false")
            .context("_typeSelect", ProjectTaskRepository.TYPE_TASK)
            .param("search-filters", "project-task-filters");

    return builder.map();
  }
}
