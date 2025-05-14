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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.ProductTaskTemplateService;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectGeneratorFactoryTaskTemplate implements ProjectGeneratorFactory {

  protected ProjectTaskRepository projectTaskRepository;
  protected ProjectRepository projectRepository;
  protected ProjectBusinessService projectBusinessService;
  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  protected ProductTaskTemplateService productTaskTemplateService;
  protected ProductCompanyService productCompanyService;
  protected AppBusinessProjectService appBusinessProjectService;
  protected SequenceService sequenceService;
  protected AppProjectService appProjectService;

  @Inject
  public ProjectGeneratorFactoryTaskTemplate(
      ProjectBusinessService projectBusinessService,
      ProjectRepository projectRepository,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      ProjectTaskRepository projectTaskRepository,
      ProductTaskTemplateService productTaskTemplateService,
      ProductCompanyService productCompanyService,
      AppBusinessProjectService appBusinessProjectService,
      SequenceService sequenceService,
      AppProjectService appProjectService) {
    this.projectBusinessService = projectBusinessService;
    this.projectRepository = projectRepository;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.projectTaskRepository = projectTaskRepository;
    this.productTaskTemplateService = productTaskTemplateService;
    this.productCompanyService = productCompanyService;
    this.appBusinessProjectService = appBusinessProjectService;
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
    List<ProjectTask> tasks = new ArrayList<>();
    List<ProjectTask> roots = new ArrayList<>();
    List<SaleOrderLine> saleOrderLineList = filterSaleOrderLinesForTasks(saleOrder);
    projectRepository.save(project);

    if (saleOrderLineList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BusinessProjectExceptionMessage.SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_2));
    }

    for (SaleOrderLine orderLine : saleOrderLineList) {
      Product product = orderLine.getProduct();
      String rootName =
          saleOrder.getSaleOrderSeq() + " - " + orderLine.getSequence() + " - " + product.getName();
      orderLine.setProject(project);
      ProjectTask root =
          projectTaskRepository
              .all()
              .filter(
                  "self.project = ? AND self.assignedTo = ? AND self.name = ? AND self.saleOrderLine = ?",
                  project,
                  project.getAssignedTo(),
                  rootName,
                  orderLine)
              .fetchOne();

      boolean isTaskGenerated =
          !projectTaskRepository
              .all()
              .filter("self.saleOrderLine = ? AND self.project = ?", orderLine, project)
              .fetch()
              .isEmpty();

      if (root == null) {
        root = projectTaskBusinessProjectService.create(rootName, project, project.getAssignedTo());
        root.setTaskDate(startDate.toLocalDate());
        if (projectTaskBusinessProjectService.isTimeUnitValid(orderLine.getUnit())) {
          updateSoldTime(root, orderLine);
        }

        productTaskTemplateService.fillProjectTask(
            project, orderLine.getQty(), orderLine, tasks, product, root, null);
        roots.add(root);
      }
      if (product != null && !isTaskGenerated) {
        if (!CollectionUtils.isEmpty(product.getTaskTemplateSet())) {
          List<ProjectTask> convertedTasks =
              productTaskTemplateService.convert(
                  product.getTaskTemplateSet().stream()
                      .filter(template -> Objects.isNull(template.getParentTaskTemplate()))
                      .collect(Collectors.toList()),
                  project,
                  root,
                  startDate,
                  orderLine.getQty(),
                  orderLine);
          tasks.addAll(convertedTasks);
        } else {
          ProjectTask childTask =
              projectTaskBusinessProjectService.create(
                  orderLine.getFullName(), project, project.getAssignedTo());
          this.updateTask(root, childTask, orderLine);

          tasks.add(projectTaskRepository.save(childTask));
        }
      } else {
        updateSoldTime(root, orderLine);
        projectTaskRepository.save(root);
        tasks.add(root);
      }
    }

    if (tasks.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BusinessProjectExceptionMessage.SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_3));
    }

    return ActionView.define(I18n.get("Tasks"))
        .model(ProjectTask.class.getName())
        .add("grid", "business-project-task-grid")
        .add("form", "business-project-task-form")
        .param("search-filters", "project-task-filters")
        .domain(
            "self.parentTask IN ("
                + roots.stream()
                    .map(root -> root.getId().toString())
                    .collect(Collectors.joining(", "))
                + ")");
  }

  protected void updateTask(ProjectTask root, ProjectTask childTask, SaleOrderLine orderLine)
      throws AxelorException {
    childTask.setParentTask(root);
    childTask.setQuantity(orderLine.getQty());
    Product product = orderLine.getProduct();
    childTask.setProduct(product);
    childTask.setUnitCost(product.getCostPrice());
    childTask.setTotalCosts(
        product.getCostPrice().multiply(orderLine.getQty()).setScale(2, RoundingMode.HALF_UP));
    childTask.setExTaxTotal(orderLine.getExTaxTotal());
    Company company =
        orderLine.getSaleOrder() != null ? orderLine.getSaleOrder().getCompany() : null;
    childTask.setUnitPrice((BigDecimal) productCompanyService.get(product, "salePrice", company));
    Unit orderLineUnit = orderLine.getUnit();
    if (projectTaskBusinessProjectService.isTimeUnitValid(orderLineUnit)) {
      childTask.setTimeUnit(orderLineUnit);
    }

    if (orderLine.getInvoicingModeSelect() == SaleOrderLineRepository.INVOICING_MODE_PACKAGE) {
      childTask.setToInvoice(true);
      childTask.setInvoicingType(ProjectTaskRepository.INVOICING_TYPE_PACKAGE);
    }
  }

  protected void updateSoldTime(ProjectTask task, SaleOrderLine saleOrderLine) {
    if (task.getSoldTime().compareTo(task.getUpdatedTime()) == 0) {
      task.setUpdatedTime(saleOrderLine.getQty());
    }
    task.setSoldTime(saleOrderLine.getQty());
  }

  protected List<SaleOrderLine> filterSaleOrderLinesForTasks(SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Product product = saleOrderLine.getProduct();
      if (product != null
          && (ProductRepository.PROCUREMENT_METHOD_PRODUCE.equals(
                  (String)
                      productCompanyService.get(
                          product, "procurementMethodSelect", saleOrder.getCompany()))
              || saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE)
          && ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())) {
        saleOrderLineList.add(saleOrderLine);
      }
    }
    return saleOrderLineList;
  }
}
