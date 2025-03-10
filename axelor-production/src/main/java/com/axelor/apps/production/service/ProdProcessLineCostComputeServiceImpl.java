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

public class ProdProcessLineCostComputeServiceImpl implements ProdProcessLineCostComputeService {

  protected final ProdProcessLineComputationService prodProcessLineComputationService;
  protected final AppProductionService appProductionService;
  protected final AppBaseService appBaseService;

  @Inject
  public ProdProcessLineCostComputeServiceImpl(
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
    switch (workCenterTypeSelect) {
      case WorkCenterRepository.WORK_CENTER_TYPE_HUMAN:
        costAmount = humanCostAmount;
        break;
      case WorkCenterRepository.WORK_CENTER_TYPE_MACHINE:
        costAmount = machineCostAmount;
        break;
      default:
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
    BigDecimal humanCostAmount;
    switch (costTypeSelect) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        humanCostAmount = costAmount;
        break;
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
        BigDecimal hrDurationPerCycle = BigDecimal.valueOf(workCenter.getHrDurationPerCycle());
        humanCostAmount =
            costAmount.divide(
                hrDurationPerCycle,
                appBaseService.getNbDecimalDigitForUnitPrice(),
                RoundingMode.HALF_UP);
        break;
      default:
        humanCostAmount = costAmount;
    }
    return humanCostAmount;
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
    BigDecimal machineCostAmount;

    switch (costTypeSelect) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        machineCostAmount = costAmount;
        break;
      case WorkCenterRepository.COST_TYPE_PER_CYCLE:
        BigDecimal nbCyclePerHour = computeCycleHourlyCost(prodProcessLine, nbCycles);
        machineCostAmount = costAmount.multiply(nbCyclePerHour);
        break;
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
        BigDecimal durationForOnePiece = computeDurationForOnePiece(prodProcessLine);
        machineCostAmount =
            costAmount.divide(
                durationForOnePiece,
                appBaseService.getNbDecimalDigitForUnitPrice(),
                RoundingMode.HALF_UP);
        break;
      default:
        machineCostAmount = costAmount;
    }
    return machineCostAmount;
  }

  private BigDecimal computeDurationForOnePiece(ProdProcessLine prodProcessLine) {
    BigDecimal maxCapacityPerCycle = prodProcessLine.getMaxCapacityPerCycle();
    BigDecimal durationPerCycle =
        BigDecimal.valueOf(prodProcessLine.getDurationPerCycle())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    return durationPerCycle.divide(
        maxCapacityPerCycle, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeCycleHourlyCost(
      ProdProcessLine prodProcessLine, BigDecimal nbCycles) {
    BigDecimal durationPerCycle =
        BigDecimal.valueOf(prodProcessLine.getDurationPerCycle())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    return nbCycles.divide(
        durationPerCycle, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
