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

  protected BigDecimal getHumanCostAmount(
      int costTypeSelect, BigDecimal costAmount, WorkCenter workCenter) {
    if (costTypeSelect == WorkCenterRepository.COST_TYPE_PER_PIECE) {
      BigDecimal hrDurationPerCycle = BigDecimal.valueOf(workCenter.getHrDurationPerCycle());
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
    return getMachineCostAmount(prodProcessLine, costTypeSelect, nbCycles, costAmount);
  }

  protected BigDecimal getMachineCostAmount(
      ProdProcessLine prodProcessLine,
      int costTypeSelect,
      BigDecimal nbCycles,
      BigDecimal costAmount) {
    if (costTypeSelect == WorkCenterRepository.COST_TYPE_PER_CYCLE) {
      BigDecimal nbCyclePerHour = computeCycleHourlyCost(prodProcessLine, nbCycles);
      return costAmount.multiply(nbCyclePerHour);
    } else if (costTypeSelect == WorkCenterRepository.COST_TYPE_PER_PIECE) {
      BigDecimal durationForOnePiece = computeDurationForOnePiece(prodProcessLine);
      return costAmount.divide(
          durationForOnePiece,
          appBaseService.getNbDecimalDigitForUnitPrice(),
          RoundingMode.HALF_UP);
    }
    return costAmount;
  }

  protected BigDecimal computeDurationForOnePiece(ProdProcessLine prodProcessLine) {
    BigDecimal maxCapacityPerCycle = prodProcessLine.getMaxCapacityPerCycle();
    BigDecimal durationPerCycle =
        prodProcessLineComputationService.getHourDurationPerCycle(prodProcessLine);
    return durationPerCycle.divide(
        maxCapacityPerCycle, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeCycleHourlyCost(
      ProdProcessLine prodProcessLine, BigDecimal nbCycles) {
    BigDecimal durationPerCycle =
        prodProcessLineComputationService.getHourDurationPerCycle(prodProcessLine);
    return nbCycles.divide(
        durationPerCycle, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
