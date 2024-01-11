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
package com.axelor.apps.businessproject.service.projectgenerator.factory;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.utils.StringTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectGeneratorFactoryTask implements ProjectGeneratorFactory {

  private ProjectBusinessService projectBusinessService;
  private ProjectRepository projectRepository;
  private ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  private ProjectTaskRepository projectTaskRepo;
  private ProductCompanyService productCompanyService;

  @Inject
  public ProjectGeneratorFactoryTask(
      ProjectBusinessService projectBusinessService,
      ProjectRepository projectRepository,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      ProjectTaskRepository projectTaskRepo,
      ProductCompanyService productCompanyService) {
    this.projectBusinessService = projectBusinessService;
    this.projectRepository = projectRepository;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.projectTaskRepo = projectTaskRepo;
    this.productCompanyService = productCompanyService;
  }

  @Override
  public Project create(SaleOrder saleOrder) {
    Project project = projectBusinessService.generateProject(saleOrder);
    project.setIsBusinessProject(true);
    return project;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ActionViewBuilder fill(Project project, SaleOrder saleOrder, LocalDateTime startDate)
      throws AxelorException {
    List<ProjectTask> tasks = new ArrayList<>();
    projectRepository.save(project);
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Product product = saleOrderLine.getProduct();
      boolean isTaskGenerated =
          projectTaskRepo
                  .all()
                  .filter("self.saleOrderLine = ? AND self.project = ?", saleOrderLine, project)
                  .fetch()
                  .size()
              > 0;
      if (product != null
          && ProductRepository.PRODUCT_TYPE_SERVICE.equals(
              (String)
                  productCompanyService.get(product, "productTypeSelect", saleOrder.getCompany()))
          && saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE
          && !(isTaskGenerated)) {

        ProjectTask task =
            projectTaskBusinessProjectService.create(
                saleOrderLine, project, project.getAssignedTo());

        if (saleOrder.getToInvoiceViaTask()) {
          task.setInvoicingType(ProjectTaskRepository.INVOICING_TYPE_PACKAGE);
        }

        task.setTaskDate(startDate.toLocalDate());
        task.setUnitPrice(
            (BigDecimal) productCompanyService.get(product, "salePrice", saleOrder.getCompany()));
        task.setExTaxTotal(saleOrderLine.getExTaxTotal());
        if (project.getIsInvoicingTimesheet()) {
          task.setToInvoice(true);
        } else {
          task.setToInvoice(false);
        }
        projectTaskRepo.save(task);
        tasks.add(task);
      }
    }
    if (tasks == null || tasks.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BusinessProjectExceptionMessage.SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_1));
    }

    return ActionView.define(String.format("Task%s generated", (tasks.size() > 1 ? "s" : "")))
        .model(ProjectTask.class.getName())
        .add("grid", "project-task-grid")
        .add("form", "project-task-form")
        .param("search-filters", "project-task-filters")
        .domain(String.format("self.id in (%s)", StringTool.getIdListString(tasks)));
  }
}
