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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.ProductTaskTemplateService;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectGeneratorFactorySubProject implements ProjectGeneratorFactory {

  protected ProjectBusinessService projectBusinessService;
  protected ProjectRepository projectRepository;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected ProductTaskTemplateService productTaskTemplateService;
  protected SequenceService sequenceService;
  protected AppProjectService appProjectService;

  @Inject
  public ProjectGeneratorFactorySubProject(
      ProjectBusinessService projectBusinessService,
      ProjectRepository projectRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      ProductTaskTemplateService productTaskTemplateService,
      SequenceService sequenceService,
      AppProjectService appProjectService) {
    this.projectBusinessService = projectBusinessService;
    this.projectRepository = projectRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.productTaskTemplateService = productTaskTemplateService;
    this.sequenceService = sequenceService;
    this.appProjectService = appProjectService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Project create(SaleOrder saleOrder) throws AxelorException {
    Project project = projectBusinessService.generateProject(saleOrder);
    project.setIsBusinessProject(true);
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

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ActionViewBuilder fill(Project project, SaleOrder saleOrder, LocalDateTime startDate)
      throws AxelorException {
    List<Project> projects = new ArrayList<>();
    projectRepository.save(project);
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

      if (SaleOrderLineRepository.TYPE_NORMAL != saleOrderLine.getTypeSelect()) {
        continue;
      }

      Product product = saleOrderLine.getProduct();
      Project lineProject = project;

      if (product != null
          && ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())
          && saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE) {
        Project phase = projectBusinessService.generatePhaseProject(saleOrderLine, project);
        lineProject = phase;
        phase.setFromDate(startDate);
        projects.add(phase);

        if (!CollectionUtils.isEmpty(product.getTaskTemplateSet())) {
          productTaskTemplateService.convert(
              product.getTaskTemplateSet().stream()
                  .filter(template -> Objects.isNull(template.getParentTaskTemplate()))
                  .collect(Collectors.toList()),
              phase,
              null,
              startDate,
              saleOrderLine.getQty(),
              saleOrderLine);
        }
      }

      saleOrderLine.setProject(lineProject);
      saleOrderLineRepository.save(saleOrderLine);
    }

    return ActionView.define(String.format("Project%s generated", (projects.size() > 1 ? "s" : "")))
        .model(Project.class.getName())
        .add("grid", "project-grid")
        .add("form", "business-project-form")
        .param("search-filters", "project-filters")
        .domain(String.format("self.id in (%s)", StringHelper.getIdListString(projects)));
  }
}
