/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.MachineToolRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderDurationRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.service.operationorder.OperationOrderPlanningService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.date.DurationHelper;
import jakarta.inject.Inject;

public class OperationOrderWorkflowServiceMaintenanceImpl
    extends OperationOrderWorkflowServiceImpl {

  @Inject
  public OperationOrderWorkflowServiceMaintenanceImpl(
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepo,
      OperationOrderDurationRepository operationOrderDurationRepo,
      AppProductionService appProductionService,
      MachineToolRepository machineToolRepo,
      ManufOrderWorkflowService manufOrderWorkflowService,
      OperationOrderService operationOrderService,
      OperationOrderPlanningService operationOrderPlanningService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderRepository manufOrderRepo) {
    super(
        operationOrderStockMoveService,
        operationOrderRepo,
        operationOrderDurationRepo,
        appProductionService,
        machineToolRepo,
        manufOrderWorkflowService,
        operationOrderService,
        operationOrderPlanningService,
        manufOrderStockMoveService,
        manufOrderRepo);
  }

  @Override
  protected void computeFinishDuration(OperationOrder operationOrder) {
    if (!Beans.get(AppBaseService.class).isApp("maintenance")
        || operationOrder.getManufOrder().getTypeSelect()
            != ManufOrderRepository.TYPE_MAINTENANCE) {
      super.computeFinishDuration(operationOrder);
      return;
    }

    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
      operationOrder.setRealDuration(
          DurationHelper.getSecondsDuration(
              operationOrderService.computeRealDuration(operationOrder)));
    }
  }
}
