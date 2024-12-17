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
package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProjectTaskGroupServiceImpl implements ProjectTaskGroupService {

  protected ProjectTaskComputeService projectTaskComputeService;
  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;

  @Inject
  public ProjectTaskGroupServiceImpl(
      ProjectTaskComputeService projectTaskComputeService,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService) {
    this.projectTaskComputeService = projectTaskComputeService;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
  }

  @Override
  public Map<String, Object> updateBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit)
      throws AxelorException {
    projectTaskComputeService.computeBudgetedTime(projectTask, oldTimeUnit);

    Map<String, Object> valuesMap = new HashMap<>(updateSoldTime(projectTask));

    valuesMap.put("budgetedTime", projectTask.getBudgetedTime());

    return valuesMap;
  }

  @Override
  public Map<String, Object> updateSoldTime(ProjectTask projectTask) throws AxelorException {
    projectTask.setSoldTime(projectTask.getBudgetedTime());

    Map<String, Object> valuesMap = new HashMap<>(updateUpdatedTime(projectTask));

    valuesMap.put("soldTime", projectTask.getSoldTime());

    return valuesMap;
  }

  @Override
  public Map<String, Object> updateUpdatedTime(ProjectTask projectTask) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    projectTask.setUpdatedTime(projectTask.getSoldTime());
    valuesMap.put("updatedTime", projectTask.getUpdatedTime());

    if (Objects.equals(ProjectTaskRepository.INVOICING_TYPE_PACKAGE, projectTask.getInvoicingType())
        && !projectTask.getInvoiced()
        && projectTask.getSaleOrderLine() == null
        && !projectTask.getIsTaskRefused()) {
      valuesMap.putAll(updateQuantity(projectTask));
    }

    return valuesMap;
  }

  @Override
  public Map<String, Object> updateQuantity(ProjectTask projectTask) throws AxelorException {
    projectTaskComputeService.computeQuantity(projectTask);

    Map<String, Object> valuesMap = new HashMap<>(updateFinancialDatas(projectTask));
    valuesMap.put("quantity", projectTask.getQuantity());

    return valuesMap;
  }

  @Override
  public Map<String, Object> updateFinancialDatas(ProjectTask projectTask) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();
    projectTaskComputeService.computeFinancialDatas(projectTask);
    projectTaskBusinessProjectService.updateDiscount(projectTask);
    projectTaskBusinessProjectService.compute(projectTask);

    valuesMap.put("unitPrice", projectTask.getUnitPrice());
    valuesMap.put("unitCost", projectTask.getUnitCost());
    valuesMap.put("discountTypeSelect", projectTask.getDiscountTypeSelect());
    valuesMap.put("discountAmount", projectTask.getDiscountAmount());
    valuesMap.put("priceDiscounted", projectTask.getPriceDiscounted());
    valuesMap.put("exTaxTotal", projectTask.getExTaxTotal());
    valuesMap.put("totalCosts", projectTask.getTotalCosts());

    return valuesMap;
  }
}
