package com.axelor.apps.businesssupport.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.projecttask.TaskTemplateBusinessProjectServiceImpl;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeComputeService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeCreateService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.google.inject.Inject;

public class TaskTemplateBusinessSupportServiceImpl extends TaskTemplateBusinessProjectServiceImpl {

  @Inject
  public TaskTemplateBusinessSupportServiceImpl(
      ProjectPlanningTimeCreateService projectPlanningTimeCreateService,
      AppBaseService appBaseService,
      ProjectPlanningTimeComputeService projectPlanningTimeComputeService,
      AppProjectService appProjectService,
      ProjectPlanningTimeRepository projectPlanningTimeRepository,
      ProductCompanyService productCompanyService,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService) {
    super(
        projectPlanningTimeCreateService,
        appBaseService,
        projectPlanningTimeComputeService,
        appProjectService,
        projectPlanningTimeRepository,
        productCompanyService,
        projectTaskBusinessProjectService);
  }

  @Override
  public void manageTemplateFields(ProjectTask task, TaskTemplate taskTemplate, Project project)
      throws AxelorException {
    super.manageTemplateFields(task, taskTemplate, project);

    task.setInternalDescription(taskTemplate.getInternalDescription());
  }
}
