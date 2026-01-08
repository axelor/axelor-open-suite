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

import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.CostSheet;
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
import java.math.RoundingMode;
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
  protected BigDecimal getHumanResourceCostDuration(
      OperationOrder operationOrder,
      CostSheetLine parentCostSheetLine,
      LocalDate previousCostSheetDate,
      BigDecimal ratio) {
    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      return super.getHumanResourceCostDuration(
          operationOrder, parentCostSheetLine, previousCostSheetDate, ratio);
    }
    BigDecimal duration = BigDecimal.ZERO;

    if (operationOrder.getTimesheetLineList() == null) {
      return duration;
    }

    BigDecimal realDuration = new BigDecimal(operationOrder.getRealDuration());
    CostSheet parentLineCostSheet = parentCostSheetLine.getCostSheet();
    if (parentLineCostSheet.getCalculationTypeSelect()
            == CostSheetRepository.CALCULATION_END_OF_PRODUCTION
        || parentLineCostSheet.getCalculationTypeSelect()
            == CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION) {
      Period period =
          previousCostSheetDate != null
              ? Period.between(parentLineCostSheet.getCalculationDate(), previousCostSheetDate)
              : null;
      duration = period != null ? new BigDecimal(period.getDays() * 24) : realDuration;
    } else if (parentLineCostSheet.getCalculationTypeSelect()
        == CostSheetRepository.CALCULATION_WORK_IN_PROGRESS) {
      BigDecimal plannedDuration = new BigDecimal(operationOrder.getPlannedDuration());
      duration = realDuration.subtract(plannedDuration.multiply(ratio));
    }
    return duration.divide(
        new BigDecimal(3600),
        appProductionService.getNbDecimalDigitForUnitPrice(),
        RoundingMode.HALF_UP);
  }
}
