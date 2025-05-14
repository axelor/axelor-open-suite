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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderCheckStockMoveLineService;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderUpdateStockMoveService;
import com.axelor.apps.production.service.operationorder.OperationOrderOutsourceService;
import com.axelor.apps.production.service.operationorder.OperationOrderServiceImpl;
import com.axelor.inject.Beans;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.util.List;

public class OperationOrderServiceBusinessImpl extends OperationOrderServiceImpl {

  @Inject
  public OperationOrderServiceBusinessImpl(
      AppProductionService appProductionService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ProdProcessLineService prodProcessLineService,
      OperationOrderRepository operationOrderRepository,
      OperationOrderOutsourceService operationOrderOutsourceService,
      ManufOrderCheckStockMoveLineService manufOrderCheckStockMoveLineService,
      ManufOrderPlanStockMoveService manufOrderPlanStockMoveService,
      ManufOrderUpdateStockMoveService manufOrderUpdateStockMoveService,
      ProdProcessLineComputationService prodProcessLineComputationService) {
    super(
        appProductionService,
        manufOrderStockMoveService,
        prodProcessLineService,
        operationOrderRepository,
        operationOrderOutsourceService,
        manufOrderCheckStockMoveLineService,
        manufOrderPlanStockMoveService,
        manufOrderUpdateStockMoveService,
        prodProcessLineComputationService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine)
      throws AxelorException {

    OperationOrder operationOrder = super.createOperationOrder(manufOrder, prodProcessLine);

    if (appProductionService.isApp("production")
        && Boolean.TRUE.equals(
            appProductionService.getAppProduction().getManageBusinessProduction())) {
      operationOrder.setIsToInvoice(manufOrder.getIsToInvoice());
    }
    operationOrder.setEmployeeSet(
        Sets.newHashSet(prodProcessLine.getWorkCenter().getHrEmployeeSet()));
    return operationOrder;
  }

  /**
   * Computes the duration of all the {@link OperationOrderDuration} of {@code operationOrder} If we
   * manage timesheet with manuf order, we get the duration with the timesheet lines.
   *
   * @param operationOrder An operation order
   * @return Real duration of {@code operationOrder}
   */
  @Override
  public Duration computeRealDuration(OperationOrder operationOrder) {

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()
        && appProductionService.getAppProduction().getEnableTimesheetOnManufOrder()) {
      List<TimesheetLine> timesheetLineList = operationOrder.getTimesheetLineList();
      return Beans.get(TimesheetLineService.class).computeTotalDuration(timesheetLineList);
    } else {
      return super.computeRealDuration(operationOrder);
    }
  }
}
