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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProdProcessLineHourlyCostComputeServiceImpl
    implements ProdProcessLineHourlyCostComputeService {

  protected final ProdProcessLineComputationService prodProcessLineComputationService;
  protected final AppProductionService appProductionService;
  protected final AppBaseService appBaseService;

  @Inject
  public ProdProcessLineHourlyCostComputeServiceImpl(
      ProdProcessLineComputationService prodProcessLineComputationService,
      AppProductionService appProductionService,
      AppBaseService appBaseService) {
    this.prodProcessLineComputationService = prodProcessLineComputationService;
    this.appProductionService = appProductionService;
    this.appBaseService = appBaseService;
  }

  @Override
  public BigDecimal computeLineHourlyCost(ProdProcessLine prodProcessLine, BigDecimal qtyToProduce)
      throws AxelorException {

    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
    BigDecimal machineCostAmount = computeMachineCostAmount(prodProcessLine, qtyToProduce);
    BigDecimal humanCostAmount = computeHumanCostAmount(prodProcessLine);
    BigDecimal nbCycles =
        prodProcessLineComputationService.getNbCycle(prodProcessLine, qtyToProduce);
    BigDecimal costAmount;
    if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_HUMAN) {
      costAmount = humanCostAmount;
    } else if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE) {
      costAmount = machineCostAmount;
    } else {
      BigDecimal humanDuration =
          prodProcessLineComputationService.getHourHumanDuration(prodProcessLine, nbCycles);
      BigDecimal machineDuration =
          prodProcessLineComputationService.getHourMachineDuration(prodProcessLine, qtyToProduce);
      if (machineDuration.compareTo(humanDuration) > 0) {
        costAmount = machineCostAmount;
      } else {
        costAmount = humanCostAmount;
      }
    }

    return costAmount;
  }

  protected BigDecimal computeHumanCostAmount(ProdProcessLine prodProcessLine) {
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    BigDecimal costAmount =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getHrCostAmount()
            : workCenter.getHrCostAmount();
    int costTypeSelect =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getHrCostTypeSelect()
            : workCenter.getHrCostTypeSelect();
    return getHumanCostAmount(costTypeSelect, costAmount, workCenter);
  }

  @Override
  public BigDecimal getHumanCostAmount(
      int costTypeSelect, BigDecimal costAmount, WorkCenter workCenter) {
    if (costTypeSelect == WorkCenterRepository.COST_TYPE_PER_PIECE) {
      BigDecimal hrDurationPerCycle =
          BigDecimal.valueOf(workCenter.getHrDurationPerCycle())
              .divide(
                  BigDecimal.valueOf(3600),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);
      return costAmount.divide(
          hrDurationPerCycle, appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    }
    return costAmount;
  }

  protected BigDecimal computeMachineCostAmount(
      ProdProcessLine prodProcessLine, BigDecimal qtyToProduce) {
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    BigDecimal costAmount =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getCostAmount()
            : workCenter.getCostAmount();
    int costTypeSelect =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getCostTypeSelect()
            : workCenter.getCostTypeSelect();
    BigDecimal nbCycles =
        prodProcessLineComputationService.getNbCycle(prodProcessLine, qtyToProduce);
    return getMachineCostAmount(
        prodProcessLine.getDurationPerCycle(),
        prodProcessLine.getMaxCapacityPerCycle(),
        costTypeSelect,
        nbCycles,
        costAmount);
  }

  @Override
  public BigDecimal getMachineCostAmount(
      long durationPerCycle,
      BigDecimal maxCapacityPerCycle,
      int costTypeSelect,
      BigDecimal nbCycles,
      BigDecimal costAmount) {
    if (costTypeSelect == WorkCenterRepository.COST_TYPE_PER_CYCLE) {
      BigDecimal nbCyclePerHour = computeCycleHourlyCost(durationPerCycle, nbCycles);
      return costAmount.multiply(nbCyclePerHour);
    } else if (costTypeSelect == WorkCenterRepository.COST_TYPE_PER_PIECE) {
      BigDecimal durationForOnePiece =
          computeDurationForOnePiece(maxCapacityPerCycle, durationPerCycle);
      return costAmount.divide(
          durationForOnePiece,
          appBaseService.getNbDecimalDigitForUnitPrice(),
          RoundingMode.HALF_UP);
    }
    return costAmount;
  }

  protected BigDecimal computeDurationForOnePiece(
      BigDecimal maxCapacityPerCycle, long durationPerCycle) {
    BigDecimal hourDurationPerCycle =
        prodProcessLineComputationService.computeHourDurationPerCycle(durationPerCycle);
    return hourDurationPerCycle.divide(
        maxCapacityPerCycle, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeCycleHourlyCost(long durationPerCycle, BigDecimal nbCycles) {
    BigDecimal hourDurationPerCycle =
        prodProcessLineComputationService.computeHourDurationPerCycle(durationPerCycle);
    return nbCycles.divide(
        hourDurationPerCycle, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
