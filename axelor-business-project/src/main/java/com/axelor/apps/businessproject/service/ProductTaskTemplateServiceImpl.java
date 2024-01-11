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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductTaskTemplateServiceImpl implements ProductTaskTemplateService {

  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  protected ProjectTaskRepository projectTaskRepo;
  protected ProductCompanyService productCompanyService;

  @Inject
  public ProductTaskTemplateServiceImpl(
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      ProjectTaskRepository projectTaskRepo,
      ProductCompanyService productCompanyService) {
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.projectTaskRepo = projectTaskRepo;
    this.productCompanyService = productCompanyService;
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
    Product product = saleOrderLine.getProduct();

    for (TaskTemplate template : templates) {
      BigDecimal qtyTmp = (template.getIsUniqueTaskForMultipleQuantity() ? BigDecimal.ONE : qty);

      while (qtyTmp.signum() > 0) {
        LocalDateTime dateWithDelay = startDate.plusHours(template.getDelayToStart().longValue());

        ProjectTask task =
            projectTaskBusinessProjectService.create(template, project, dateWithDelay, qty);
        task.setParentTask(parent);
        task.setProduct(product);
        task.setQuantity(!template.getIsUniqueTaskForMultipleQuantity() ? BigDecimal.ONE : qty);
        task.setUnit(product.getUnit());
        task.setUnitPrice(
            (BigDecimal) productCompanyService.get(product, "salePrice", project.getCompany()));
        task.setExTaxTotal(task.getUnitPrice().multiply(task.getQuantity()));
        if (saleOrderLine.getSaleOrder().getToInvoiceViaTask()) {
          task.setToInvoice(true);
          task.setInvoicingType(ProjectTaskRepository.INVOICING_TYPE_PACKAGE);
        }
        tasks.add(projectTaskRepo.save(task));

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

        qtyTmp = qtyTmp.subtract(BigDecimal.ONE);
      }
    }

    return tasks;
  }
}
