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
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class ProjectTaskComputeBusinessServiceImpl implements ProjectTaskComputeBusinessService {

  protected ProjectFrameworkContractService projectFrameworkContractService;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected ProjectTimeUnitService projectTimeUnitService;
  public static final int COMPUTE_SCALE = 5;

  @Inject
  public ProjectTaskComputeBusinessServiceImpl(
      ProjectFrameworkContractService projectFrameworkContractService,
      UnitConversionForProjectService unitConversionForProjectService,
      ProjectTimeUnitService projectTimeUnitService) {
    this.projectFrameworkContractService = projectFrameworkContractService;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.projectTimeUnitService = projectTimeUnitService;
  }

  @Override
  public void computeQuantity(ProjectTask projectTask) throws AxelorException {
    Unit unit = projectTimeUnitService.getTaskDefaultHoursTimeUnit(projectTask);
    if (projectTask == null || projectTask.getInvoicingUnit() == null || unit == null) {
      return;
    }

    projectTask.setQuantity(
        unitConversionForProjectService.convert(
            unit,
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
