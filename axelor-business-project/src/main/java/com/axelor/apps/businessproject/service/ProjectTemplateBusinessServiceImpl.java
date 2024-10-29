/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
  protected boolean isWizardNeeded(ProjectTemplate projectTemplate) {
    return super.isWizardNeeded(projectTemplate) && !projectTemplate.getIsBusinessProject();
  }

  @Override
  protected Map<String, Object> computeWizardContext(ProjectTemplate projectTemplate) {
    Map<String, Object> contextMap = super.computeWizardContext(projectTemplate);
    contextMap.put("_businessProject", projectTemplate.getIsBusinessProject());

    return contextMap;
  }
}
