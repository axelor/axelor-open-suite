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
import java.time.Period;
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

    if (operationOrder.getTimesheetLineList() != null) {
      if (parentCostSheetLine.getCostSheet().getCalculationTypeSelect()
              == CostSheetRepository.CALCULATION_END_OF_PRODUCTION
          || parentCostSheetLine.getCostSheet().getCalculationTypeSelect()
              == CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION) {
        Period period =
            previousCostSheetDate != null
                ? Period.between(
                    parentCostSheetLine.getCostSheet().getCalculationDate(), previousCostSheetDate)
                : null;
        duration =
            period != null
                ? new BigDecimal(period.getDays() * 24)
                : new BigDecimal(operationOrder.getRealDuration());
      } else if (parentCostSheetLine.getCostSheet().getCalculationTypeSelect()
          == CostSheetRepository.CALCULATION_WORK_IN_PROGRESS) {

        BigDecimal ratio = costSheet.getManufOrderProducedRatio();

        /*
         * Using BigDecimal value of plannedDuration and realDuration for calculation with manufOrderProducedRatio
         */
        duration =
            (new BigDecimal(operationOrder.getRealDuration()))
                .subtract((new BigDecimal(operationOrder.getPlannedDuration()).multiply(ratio)));
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
