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
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.CostSheetLineService;
import com.axelor.apps.production.service.costsheet.CostSheetServiceImpl;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostSheetServiceBusinessImpl extends CostSheetServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public CostSheetServiceBusinessImpl(
      ProdProcessLineComputationService prodProcessLineComputationService,
      AppProductionService appProductionService,
      AppBaseService appBaseService,
      BillOfMaterialRepository billOfMaterialRepo,
      CostSheetLineService costSheetLineService,
      UnitConversionService unitConversionService) {
    super(
        prodProcessLineComputationService,
        appProductionService,
        appBaseService,
        billOfMaterialRepo,
        costSheetLineService,
        unitConversionService);
  }

  @Override
  protected void computeRealHumanResourceCost(
      OperationOrder operationOrder,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      LocalDate previousCostSheetDate)
      throws AxelorException {

    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      super.computeRealHumanResourceCost(
          operationOrder, priority, bomLevel, parentCostSheetLine, previousCostSheetDate);
      return;
    }
    BigDecimal duration =
        BigDecimal.ZERO; // Declaring duration as BigDecimal to use it with manufOrderProducedRatio
    BigDecimal realDuration = BigDecimal.valueOf(operationOrder.getRealDuration());
    int calculationType = parentCostSheetLine.getCostSheet().getCalculationTypeSelect();

    if (operationOrder.getTimesheetLineList() != null) {
      if (calculationType == CostSheetRepository.CALCULATION_END_OF_PRODUCTION
          || calculationType == CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION) {
        duration =
            computeDurationBetweenCostSheets(
                previousCostSheetDate,
                parentCostSheetLine.getCostSheet().getCalculationDate(),
                realDuration);
      } else if (calculationType == CostSheetRepository.CALCULATION_WORK_IN_PROGRESS) {
        BigDecimal ratio = costSheet.getManufOrderProducedRatio();
        BigDecimal plannedDuration =
            BigDecimal.valueOf(operationOrder.getPlannedDuration()).multiply(ratio);
        duration = realDuration.subtract(plannedDuration).abs();
      }

      // TODO get the timesheet Line done when we run the calculation.

      this.computeRealHumanResourceCost(
          operationOrder.getProdProcessLine(),
          operationOrder.getWorkCenter(),
          priority,
          bomLevel,
          parentCostSheetLine,
          duration);
    }
  }
}
