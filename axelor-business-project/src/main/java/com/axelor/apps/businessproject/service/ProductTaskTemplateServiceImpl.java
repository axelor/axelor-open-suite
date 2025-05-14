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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductTaskTemplateServiceImpl implements ProductTaskTemplateService {

  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  protected ProjectTaskRepository projectTaskRepo;
  protected ProductCompanyService productCompanyService;
  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public ProductTaskTemplateServiceImpl(
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      ProjectTaskRepository projectTaskRepo,
      ProductCompanyService productCompanyService,
      AppBusinessProjectService appBusinessProjectService) {
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.projectTaskRepo = projectTaskRepo;
    this.productCompanyService = productCompanyService;
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<ProjectTask> convert(
      List<? extends TaskTemplate> templates,
      Project project,
      ProjectTask parent,
      LocalDateTime startDate,
      BigDecimal qty,
      SaleOrderLine saleOrderLine)
      throws AxelorException {
    List<ProjectTask> tasks = new ArrayList<>();

    BigDecimal taskQty;

    for (TaskTemplate template : templates) {
      Product product = template.getProduct();

      BigDecimal templateQty = template.getQty();
      if (templateQty.signum() <= 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(BusinessProjectExceptionMessage.PROJECT_TASK_TEMPLATE_QUANTITY_ERROR),
                template.getName()));
      }
      taskQty = qty.multiply(templateQty).setScale(2, RoundingMode.HALF_UP);

      BigDecimal numberOfTasks =
          template.getIsUniqueTaskForMultipleQuantity() ? BigDecimal.ONE : taskQty;

      if (!template.getIsUniqueTaskForMultipleQuantity()) {
        taskQty = BigDecimal.ONE;
      }

      while (numberOfTasks.signum() > 0) {
        LocalDateTime dateWithDelay = startDate.plusHours(template.getDelayToStart().longValue());

        ProjectTask task =
            projectTaskBusinessProjectService.create(template, project, dateWithDelay, taskQty);
        task.setName(saleOrderLine.getSaleOrder().getSaleOrderSeq() + " - " + task.getName());

        fillProjectTask(project, taskQty, saleOrderLine, tasks, product, task, parent);

        // Only parent task can have multiple quantities
        List<ProjectTask> children =
            convert(
                template.getTaskTemplateList(),
                project,
                task,
                dateWithDelay,
                BigDecimal.ONE,
                saleOrderLine);
        tasks.addAll(children);

        numberOfTasks = numberOfTasks.subtract(BigDecimal.ONE);
      }
    }
    return tasks;
  }

  @Override
  public void fillProjectTask(
      Project project,
      BigDecimal qty,
      SaleOrderLine saleOrderLine,
      List<ProjectTask> tasks,
      Product product,
      ProjectTask task,
      ProjectTask parent)
      throws AxelorException {
    task.setParentTask(parent);
    if (product == null) {
      product = saleOrderLine.getProduct();
    }
    task.setProduct(product);
    BigDecimal costPrice = product.getCostPrice();
    task.setUnitCost(costPrice);
    task.setTotalCosts(costPrice.multiply(qty).setScale(2, RoundingMode.HALF_UP));
    task.setInvoicingUnit(product.getUnit());
    task.setCurrency(product.getSaleCurrency());
    task.setUnitPrice(saleOrderLine.getPrice());

    if (Objects.isNull(parent)) {
      task.setSaleOrderLine(saleOrderLine);
    }

    task.setQuantity(qty);

    Unit orderLineUnit = saleOrderLine.getUnit();
    if (projectTaskBusinessProjectService.isTimeUnitValid(orderLineUnit)) {
      task.setTimeUnit(orderLineUnit);
    }

    task.setExTaxTotal(task.getUnitPrice().multiply(task.getQuantity()));
    if (saleOrderLine.getInvoicingModeSelect() == SaleOrderLineRepository.INVOICING_MODE_PACKAGE) {
      task.setToInvoice(true);
      task.setInvoicingType(ProjectTaskRepository.INVOICING_TYPE_PACKAGE);
    }
    tasks.add(projectTaskRepo.save(task));
  }
}
