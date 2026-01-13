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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
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
    return computeNbCycle(qty, maxCapacityPerCycle);
  }

  @Override
  public BigDecimal computeNbCycle(BigDecimal qty, BigDecimal maxCapacityPerCycle) {
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

    return computeMachineInstallingDuration(
        nbCycles,
        prodProcessLine.getStartingDuration(),
        prodProcessLine.getEndingDuration(),
        prodProcessLine.getSetupDuration());
  }

  @Override
  public BigDecimal computeMachineInstallingDuration(
      BigDecimal nbCycles, long startingDuration, long endingDuration, long setupDuration) {
    BigDecimal cycleSetupDuration =
        nbCycles.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(setupDuration));
    return BigDecimal.valueOf(startingDuration + endingDuration).add(cycleSetupDuration);
  }

  @Override
  public BigDecimal getHourMachineDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException {
    return getMachineDuration(prodProcessLine, nbCycles)
        .divide(BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
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
      return computeMachineDuration(
          nbCycles,
          prodProcessLine.getDurationPerCycle(),
          getMachineInstallingDuration(prodProcessLine, nbCycles));
    }

    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal computeMachineDuration(
      BigDecimal nbCycle, long durationPerCycle, BigDecimal machineInstallingDuration) {
    return nbCycle.multiply(BigDecimal.valueOf(durationPerCycle)).add(machineInstallingDuration);
  }

  @Override
  public BigDecimal getHumanDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException {
    Objects.requireNonNull(prodProcessLine);
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    if (workCenter == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_LINE_MISSING_WORK_CENTER),
          prodProcessLine.getProdProcess() != null
              ? prodProcessLine.getProdProcess().getCode()
              : "null",
          prodProcessLine.getName());
    }
    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
    if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_HUMAN
        || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      return nbCycles.multiply(BigDecimal.valueOf(prodProcessLine.getHumanDuration()));
    }

    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getHourHumanDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException {
    return getHumanDuration(prodProcessLine, nbCycles)
        .divide(BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
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

  @Override
  public BigDecimal getHourTotalDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException {
    return getTotalDuration(prodProcessLine, nbCycles)
        .divide(BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }

  @Override
  public long computeEntireCycleDuration(
      OperationOrder operationOrder, ProdProcessLine prodProcessLine, BigDecimal qty)
      throws AxelorException {
    BigDecimal nbCycles = this.getNbCycle(prodProcessLine, qty);
    BigDecimal humanDuration = this.getHumanDuration(prodProcessLine, nbCycles);
    BigDecimal machineDuration = this.getMachineDuration(prodProcessLine, nbCycles);
    BigDecimal totalDuration = this.getTotalDuration(prodProcessLine, nbCycles);

    if (operationOrder != null) {
      operationOrder.setPlannedMachineDuration(machineDuration.longValue());
      operationOrder.setPlannedHumanDuration(humanDuration.longValue());
    }

    return totalDuration.longValue();
  }

  @Override
  public BigDecimal getHourDurationPerCycle(ProdProcessLine prodProcessLine) {
    return BigDecimal.valueOf(prodProcessLine.getDurationPerCycle())
        .divide(BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
