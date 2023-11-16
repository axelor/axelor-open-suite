/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.operationorder.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.google.inject.Inject;

public abstract class OperationOrderPlanningCommonService {

  protected OperationOrderService operationOrderService;
  protected OperationOrderStockMoveService operationOrderStockMoveService;
  protected OperationOrderRepository operationOrderRepository;

  @Inject
  protected OperationOrderPlanningCommonService(
      OperationOrderService operationOrderService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepository) {
    this.operationOrderService = operationOrderService;
    this.operationOrderStockMoveService = operationOrderStockMoveService;
    this.operationOrderRepository = operationOrderRepository;
  }

  protected abstract void planWithStrategy(OperationOrder operationOrder) throws AxelorException;

  public OperationOrder plan(OperationOrder operationOrder) throws AxelorException {

    planWithStrategy(operationOrder);

    ManufOrder manufOrder = operationOrder.getManufOrder();
    if (manufOrder != null && Boolean.TRUE.equals(manufOrder.getIsConsProOnOperation())) {
      operationOrderStockMoveService.createToConsumeStockMove(operationOrder);
    }

    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_PLANNED);
    return operationOrderRepository.save(operationOrder);
  }
}
