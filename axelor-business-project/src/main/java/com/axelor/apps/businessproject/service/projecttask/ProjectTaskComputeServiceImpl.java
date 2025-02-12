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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.businessproject.service.ProjectFrameworkContractService;
import com.axelor.apps.hr.service.UnitConversionForProjectService;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class ProjectTaskComputeServiceImpl implements ProjectTaskComputeService {

  protected UnitConversionForProjectService unitConversionForProjectService;
  protected ProjectFrameworkContractService projectFrameworkContractService;
  public static final int COMPUTE_SCALE = 5;

  @Inject
  public ProjectTaskComputeServiceImpl(
      UnitConversionForProjectService unitConversionForProjectService,
      ProjectFrameworkContractService projectFrameworkContractService) {
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.projectFrameworkContractService = projectFrameworkContractService;
  }

  @Override
  public void computeBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit)
      throws AxelorException {
    if (projectTask == null
        || oldTimeUnit == null
        || projectTask.getTimeUnit() == null
        || projectTask.getProject() == null) {
      return;
    }

    projectTask.setBudgetedTime(
        unitConversionForProjectService.convert(
            oldTimeUnit,
            projectTask.getTimeUnit(),
            projectTask.getBudgetedTime(),
            COMPUTE_SCALE,
            projectTask.getProject()));
  }

  @Override
  public void computeQuantity(ProjectTask projectTask) throws AxelorException {
    if (projectTask == null
        || projectTask.getInvoicingUnit() == null
        || projectTask.getTimeUnit() == null) {
      return;
    }

    projectTask.setQuantity(
        unitConversionForProjectService.convert(
            projectTask.getTimeUnit(),
            projectTask.getInvoicingUnit(),
            projectTask.getUpdatedTime(),
            COMPUTE_SCALE,
            projectTask.getProject()));
  }

  @Override
  public void computeFinancialDatas(ProjectTask projectTask) throws AxelorException {
    if (projectTask == null || projectTask.getProduct() == null) {
      return;
    }
    Map<String, Object> productDatas =
        projectFrameworkContractService.getProductDataFromContract(projectTask);
    Product product = projectTask.getProduct();
    Unit productUnit = Optional.of(product).map(Product::getSalesUnit).orElse(product.getUnit());

    if (productDatas.get("unitPrice") != null) {
      projectTask.setUnitPrice(
          unitConversionForProjectService.convert(
              projectTask.getInvoicingUnit(),
              productUnit,
              (BigDecimal) productDatas.get("unitPrice"),
              COMPUTE_SCALE,
              projectTask.getProject()));
    }
    if (productDatas.get("unitCost") != null) {
      projectTask.setUnitCost(
          unitConversionForProjectService.convert(
              projectTask.getInvoicingUnit(),
              productUnit,
              (BigDecimal) productDatas.get("unitCost"),
              COMPUTE_SCALE,
              projectTask.getProject()));
    }
  }
}
