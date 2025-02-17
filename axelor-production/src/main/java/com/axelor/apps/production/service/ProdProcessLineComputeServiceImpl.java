package com.axelor.apps.production.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProdProcessLineComputeServiceImpl implements ProdProcessLineComputeService {

  @Override
  public BigDecimal computeMachineDuration(
      ProdProcessLine prodProcessLine, BigDecimal qtyToProduce) {
    BigDecimal maxCapacityPerCycle = prodProcessLine.getMaxCapacityPerCycle();
    BigDecimal nbCycles = getNbCycles(maxCapacityPerCycle, qtyToProduce);
    BigDecimal durationPerCycleDecimal = prodProcessLine.getDurationPerCycleDecimal();
    BigDecimal setupDuration =
        BigDecimal.valueOf(prodProcessLine.getSetupDuration())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    BigDecimal startingDuration =
        BigDecimal.valueOf(prodProcessLine.getStartingDuration())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    BigDecimal endingDuration =
        BigDecimal.valueOf(prodProcessLine.getEndingDuration())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);

    return durationPerCycleDecimal
        .multiply(nbCycles)
        .add((setupDuration.multiply(nbCycles.subtract(BigDecimal.ONE))))
        .add(startingDuration)
        .add(endingDuration);
  }

  @Override
  public BigDecimal getNbCycles(BigDecimal maxCapacityPerCycle, BigDecimal qtyToProduce) {
    if (maxCapacityPerCycle.compareTo(BigDecimal.ZERO) == 0) {
      return qtyToProduce;
    } else {
      return qtyToProduce.divide(maxCapacityPerCycle, 0, RoundingMode.UP);
    }
  }
}
