package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.repo.ProjectTemplateRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.project.service.ProjectTemplateServiceImpl;
import com.axelor.apps.project.service.TaskTemplateService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.utils.db.Wizard;
import com.google.inject.Inject;
import java.util.Map;

public class ProjectTemplateBusinessServiceImpl extends ProjectTemplateServiceImpl {

  @Inject
  public ProjectTemplateBusinessServiceImpl(
      ProjectTemplateRepository projectTemplateRepo,
      TaskTemplateService taskTemplateService,
      ProjectService projectService,
      AppProjectService appProjectService) {
    super(projectTemplateRepo, taskTemplateService, projectService, appProjectService);
  }

  @Override
  public Map<String, Object> createProjectFromTemplateView(ProjectTemplate projectTemplate)
      throws AxelorException {
    if (appProjectService.getAppProject().getGenerateProjectSequence()
        && !projectTemplate.getIsBusinessProject()) {
      projectTemplate = projectTemplateRepo.find(projectTemplate.getId());
      Project project = projectService.createProjectFromTemplate(projectTemplate, null, null);
      return ActionView.define(I18n.get("Project"))
          .model(Project.class.getName())
          .add("form", "project-form")
          .add("grid", "project-grid")
          .param("search-filters", "project-filters")
          .context("_showRecord", project.getId())
          .map();
    }

    return ActionView.define(I18n.get("Create project from this template"))
        .model(Wizard.class.getName())
        .add("form", "project-template-wizard-form")
        .param("popup", "reload")
        .param("show-toolbar", "false")
        .param("show-confirm", "false")
        .param("width", "large")
        .param("popup-save", "false")
        .context("_projectTemplate", projectTemplate)
        .context("_businessProject", projectTemplate.getIsBusinessProject())
        .map();
  }
}
