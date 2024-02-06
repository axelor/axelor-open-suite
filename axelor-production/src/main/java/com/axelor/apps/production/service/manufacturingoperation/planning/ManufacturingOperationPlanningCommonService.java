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
package com.axelor.apps.production.service.manufacturingoperation.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationStockMoveService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public abstract class ManufacturingOperationPlanningCommonService {

  protected ManufacturingOperationService manufacturingOperationService;
  protected ManufacturingOperationStockMoveService manufacturingOperationStockMoveService;
  protected ManufacturingOperationRepository manufacturingOperationRepository;
  protected AppBaseService appBaseService;

  @Inject
  protected ManufacturingOperationPlanningCommonService(
      ManufacturingOperationService manufacturingOperationService,
      ManufacturingOperationStockMoveService manufacturingOperationStockMoveService,
      ManufacturingOperationRepository manufacturingOperationRepository,
      AppBaseService appBaseService) {
    this.manufacturingOperationService = manufacturingOperationService;
    this.manufacturingOperationStockMoveService = manufacturingOperationStockMoveService;
    this.manufacturingOperationRepository = manufacturingOperationRepository;
    this.appBaseService = appBaseService;
  }

  protected abstract void planWithStrategy(ManufacturingOperation manufacturingOperation)
      throws AxelorException;

  public ManufacturingOperation plan(ManufacturingOperation manufacturingOperation)
      throws AxelorException {

    planWithStrategy(manufacturingOperation);

    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    if (manufOrder != null && Boolean.TRUE.equals(manufOrder.getIsConsProOnOperation())) {
      manufacturingOperationStockMoveService.createToConsumeStockMove(manufacturingOperation);
    }

    manufacturingOperation.setStatusSelect(ManufacturingOperationRepository.STATUS_PLANNED);
    return manufacturingOperationRepository.save(manufacturingOperation);
  }

  /**
   * For planning at the latest we check if the manufacturingOperation plannedStartDateT is before
   * current date time.
   *
   * @param manufacturingOperation
   * @throws AxelorException
   */
  public void checkIfPlannedStartDateTimeIsBeforeCurrentDateTime(
      ManufacturingOperation manufacturingOperation) throws AxelorException {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    LocalDateTime todayDateT =
        appBaseService.getTodayDateTime(manufOrder.getCompany()).toLocalDateTime();

    if (manufacturingOperation.getPlannedStartDateT().isBefore(todayDateT)) {

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
