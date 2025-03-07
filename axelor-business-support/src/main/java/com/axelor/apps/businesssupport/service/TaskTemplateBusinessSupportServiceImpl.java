/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
