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
package com.axelor.apps.businessproject.service.projectgenerator.factory;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectGeneratorSaleServiceImpl implements ProjectGeneratorSaleService {

  protected ProjectBusinessService projectBusinessService;
  protected ProjectRepository projectRepository;
  protected SequenceService sequenceService;
  protected AppProjectService appProjectService;
  protected ProjectService projectService;

  @Inject
  public ProjectGeneratorSaleServiceImpl(
      ProjectBusinessService projectBusinessService,
      ProjectRepository projectRepository,
      SequenceService sequenceService,
      AppProjectService appProjectService,
      ProjectService projectService) {
    this.projectBusinessService = projectBusinessService;
    this.projectRepository = projectRepository;
    this.sequenceService = sequenceService;
    this.appProjectService = appProjectService;
    this.projectService = projectService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Project create(SaleOrder saleOrder, ProjectTemplate projectTemplate)
      throws AxelorException {
    if (saleOrder == null) {
      return null;
    }

    Project project = generateProject(saleOrder, projectTemplate);

    project.setIsBusinessProject(true);
    saleOrder.setProject(project);
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      saleOrderLine.setProject(project);
    }
    project = projectRepository.save(project);
    try {
      if (!appProjectService.getAppProject().getGenerateProjectSequence()) {
        project.setCode(sequenceService.getDraftSequenceNumber(project));
      }
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }
    return project;
  }

  protected Project generateProject(SaleOrder saleOrder, ProjectTemplate projectTemplate)
      throws AxelorException {
    Project project = null;

    if (projectTemplate == null) {
      project = projectBusinessService.generateProject(saleOrder);
    } else {
      String projectCode = saleOrder.getFullName() + "_project";
      project = projectRepository.findByName(projectCode);
      if (project == null) {
        project =
            projectService.createProjectFromTemplate(
                projectTemplate, projectCode, saleOrder.getClientPartner());
        project.setAssignedTo(saleOrder.getSalespersonUser());
      }
    }

    return project;
  }
}
