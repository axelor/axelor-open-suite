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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public abstract class OperationOrderPlanningCommonService {

  protected OperationOrderService operationOrderService;
  protected OperationOrderStockMoveService operationOrderStockMoveService;
  protected OperationOrderRepository operationOrderRepository;
  protected AppBaseService appBaseService;

  @Inject
  protected OperationOrderPlanningCommonService(
      OperationOrderService operationOrderService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepository,
      AppBaseService appBaseService) {
    this.operationOrderService = operationOrderService;
    this.operationOrderStockMoveService = operationOrderStockMoveService;
    this.operationOrderRepository = operationOrderRepository;
    this.appBaseService = appBaseService;
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

  /**
   * For planning at the latest we check if the operationOrder plannedStartDateT is before current
   * date time.
   *
   * @param operationOrder
   * @throws AxelorException
   */
  public void checkIfPlannedStartDateTimeIsBeforeCurrentDateTime(OperationOrder operationOrder)
      throws AxelorException {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    LocalDateTime todayDateT =
        appBaseService.getTodayDateTime(manufOrder.getCompany()).toLocalDateTime();

    if (operationOrder.getPlannedStartDateT().isBefore(todayDateT)) {

      int qtyScale = appBaseService.getNbDecimalDigitForQty();
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PLAN_IS_BEFORE_TODAY_DATE),
          String.format(
              "%s %s",
              manufOrder.getQty() != null
                  ? manufOrder.getQty().setScale(qtyScale, RoundingMode.HALF_UP)
                  : null,
              manufOrder.getProduct().getFullName()));
    }
  }
}
