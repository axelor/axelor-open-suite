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
package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeComputeService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeCreateService;
import com.axelor.apps.hr.service.project.TaskTemplateHrServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;

public class TaskTemplateBusinessProjectServiceImpl extends TaskTemplateHrServiceImpl {

  protected ProductCompanyService productCompanyService;
  protected AppBaseService appBaseService;
  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;

  @Inject
  public TaskTemplateBusinessProjectServiceImpl(
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
        projectPlanningTimeRepository);
    this.productCompanyService = productCompanyService;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
  }

  @Override
  public void manageTemplateFields(ProjectTask task, TaskTemplate taskTemplate, Project project)
      throws AxelorException {
    super.manageTemplateFields(task, taskTemplate, project);

    Product product = task.getProduct();
    task.setBudgetedTime(taskTemplate.getDuration());
    task.setPlannedTime(taskTemplate.getTotalPlannedHrs());
    if (product != null && project.getIsBusinessProject()) {
      Unit unit = (Unit) productCompanyService.get(product, "salesUnit", project.getCompany());
      if (Objects.isNull(unit)) {
        unit = (Unit) productCompanyService.get(product, "unit", project.getCompany());
      }
      task.setInvoicingUnit(unit);
      task.setUnitPrice(
          (BigDecimal) productCompanyService.get(product, "salePrice", project.getCompany()));
      task.setCurrency(
          (Currency) productCompanyService.get(product, "saleCurrency", project.getCompany()));
      task.setUnitCost(product.getCostPrice());
      projectTaskBusinessProjectService.compute(task);
      task.setSoldTime(task.getBudgetedTime());
      task.setUpdatedTime(task.getBudgetedTime());
    }
  }
}
