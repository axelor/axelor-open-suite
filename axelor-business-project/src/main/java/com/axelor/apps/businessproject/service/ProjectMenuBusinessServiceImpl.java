package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.Project;
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
}
