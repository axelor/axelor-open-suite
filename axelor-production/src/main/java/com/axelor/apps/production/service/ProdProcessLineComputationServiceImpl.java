package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ProdProcessLineComputationServiceImpl implements ProdProcessLineComputationService {
  @Override
  public BigDecimal getNbCycle(ProdProcessLine prodProcessLine, BigDecimal qty) {
    Objects.requireNonNull(prodProcessLine);
    BigDecimal maxCapacityPerCycle = prodProcessLine.getMaxCapacityPerCycle();

    BigDecimal nbCycles;
    if (maxCapacityPerCycle.compareTo(BigDecimal.ZERO) == 0) {
      nbCycles = qty;
    } else {
      nbCycles = qty.divide(maxCapacityPerCycle, 0, RoundingMode.UP);
    }
    return nbCycles;
  }

  @Override
  public BigDecimal getMachineInstallingDuration(
      ProdProcessLine prodProcessLine, BigDecimal nbCycles) throws AxelorException {
    Objects.requireNonNull(prodProcessLine);

    BigDecimal setupDuration =
        nbCycles
            .subtract(BigDecimal.ONE)
            .multiply(BigDecimal.valueOf(prodProcessLine.getSetupDuration()));
    return BigDecimal.valueOf(
            prodProcessLine.getStartingDuration() + prodProcessLine.getEndingDuration())
        .add(setupDuration);
  }

  @Override
  public BigDecimal getMachineDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException {
    Objects.requireNonNull(prodProcessLine);

    if (prodProcessLine.getWorkCenter() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_LINE_MISSING_WORK_CENTER),
          prodProcessLine.getProdProcess() != null
              ? prodProcessLine.getProdProcess().getCode()
              : "null",
          prodProcessLine.getName());
    }

    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
    if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      Machine machine = workCenter.getMachine();
      if (machine == null) {
        throw new AxelorException(
            workCenter,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(ProductionExceptionMessage.WORKCENTER_NO_MACHINE),
            workCenter.getName());
      }

      return nbCycles
          .multiply(BigDecimal.valueOf(prodProcessLine.getDurationPerCycle()))
          .add(getMachineInstallingDuration(prodProcessLine, nbCycles));
    }

    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getHumanDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles) {
    Objects.requireNonNull(prodProcessLine);
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
    if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_HUMAN
        || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      return nbCycles.multiply(BigDecimal.valueOf(prodProcessLine.getHumanDuration()));
    }

    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getTotalDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException {
    Objects.requireNonNull(prodProcessLine);

    BigDecimal machineDuration = getMachineDuration(prodProcessLine, nbCycles);
    BigDecimal humanDuration = getHumanDuration(prodProcessLine, nbCycles);

    if (humanDuration.compareTo(machineDuration) > 0) {
      return humanDuration;
    }
    return machineDuration;
  }
}
