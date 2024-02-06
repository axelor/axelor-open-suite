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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ManufacturingOperationDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationOutsourceService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.inject.Beans;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.util.List;

public class ManufacturingOperationServiceBusinessImpl extends ManufacturingOperationServiceImpl {

  @Inject
  public ManufacturingOperationServiceBusinessImpl(
      BarcodeGeneratorService barcodeGeneratorService,
      AppProductionService appProductionService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ProdProcessLineService prodProcessLineService,
      ManufacturingOperationRepository manufacturingOperationRepository,
      ManufacturingOperationOutsourceService manufacturingOperationOutsourceService) {
    super(
        barcodeGeneratorService,
        appProductionService,
        manufOrderStockMoveService,
        prodProcessLineService,
        manufacturingOperationRepository,
        manufacturingOperationOutsourceService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ManufacturingOperation createManufacturingOperation(
      ManufOrder manufOrder, ProdProcessLine prodProcessLine) throws AxelorException {

    ManufacturingOperation manufacturingOperation =
        super.createManufacturingOperation(manufOrder, prodProcessLine);

    if (appProductionService.isApp("production")
        && Boolean.TRUE.equals(
            appProductionService.getAppProduction().getManageBusinessProduction())) {
      manufacturingOperation.setIsToInvoice(manufOrder.getIsToInvoice());
    }
    manufacturingOperation.setEmployeeSet(
        Sets.newHashSet(prodProcessLine.getWorkCenter().getHrEmployeeSet()));
    return manufacturingOperation;
  }

  /**
   * Computes the duration of all the {@link ManufacturingOperationDuration} of {@code
   * manufacturingOperation} If we manage timesheet with manuf order, we get the duration with the
   * timesheet lines.
   *
   * @param manufacturingOperation An operation order
   * @return Real duration of {@code manufacturingOperation}
   */
  @Override
  public Duration computeRealDuration(ManufacturingOperation manufacturingOperation) {

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()
        && appProductionService.getAppProduction().getEnableTimesheetOnManufOrder()) {
      List<TimesheetLine> timesheetLineList = manufacturingOperation.getTimesheetLineList();
      return Beans.get(TimesheetLineService.class).computeTotalDuration(timesheetLineList);
    } else {
      return super.computeRealDuration(manufacturingOperation);
    }
  }
}
