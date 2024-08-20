package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.repo.ProjectTemplateRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.project.service.ProjectTemplateServiceImpl;
import com.axelor.apps.project.service.TaskTemplateService;
import com.axelor.apps.project.service.app.AppProjectService;
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
  public boolean isWizardNeeded(ProjectTemplate projectTemplate) {
    return super.isWizardNeeded(projectTemplate) && !projectTemplate.getIsBusinessProject();
  }

  @Override
  public Map<String, Object> computeWizardContext(ProjectTemplate projectTemplate) {
    Map<String, Object> contextMap = super.computeWizardContext(projectTemplate);
    contextMap.put("_businessProject", projectTemplate.getIsBusinessProject());

    return contextMap;
  }
}
