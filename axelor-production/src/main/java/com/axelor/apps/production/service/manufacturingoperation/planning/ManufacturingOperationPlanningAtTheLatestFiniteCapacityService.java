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
package com.axelor.apps.production.service.manufacturingoperation.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationStockMoveService;
import com.axelor.utils.helpers.date.DurationHelper;
import com.axelor.utils.helpers.date.LocalDateTimeHelper;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ManufacturingOperationPlanningAtTheLatestFiniteCapacityService
    extends ManufacturingOperationPlanningCommonService {

  protected MachineService machineService;

  @Inject
  protected ManufacturingOperationPlanningAtTheLatestFiniteCapacityService(
      ManufacturingOperationService manufacturingOperationService,
      ManufacturingOperationStockMoveService manufacturingOperationStockMoveService,
      ManufacturingOperationRepository manufacturingOperationRepository,
      AppBaseService appBaseService,
      MachineService machineService) {
    super(
        manufacturingOperationService,
        manufacturingOperationStockMoveService,
        manufacturingOperationRepository,
        appBaseService);
    this.machineService = machineService;
  }

  @Override
  protected void planWithStrategy(ManufacturingOperation manufacturingOperation)
      throws AxelorException {

    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    LocalDateTime todayDateT =
        appBaseService.getTodayDateTime(manufOrder.getCompany()).toLocalDateTime();

    LocalDateTime plannedEndDate = manufacturingOperation.getPlannedEndDateT();
    LocalDateTime plannedStartDate = manufacturingOperation.getPlannedStartDateT();
    Long plannedDuration = manufacturingOperation.getPlannedDuration();

    List<Machine> machines = getEligibleMachines(manufacturingOperation);
    for (Machine machine : machines) {
      manufacturingOperation.setMachine(machine);
      manufacturingOperation.setPlannedEndDateT(plannedEndDate);
      manufacturingOperation.setPlannedStartDateT(plannedStartDate);
      manufacturingOperation.setPlannedDuration(plannedDuration);
      planWithStrategyAndMachine(manufacturingOperation, machine);

      if (manufacturingOperation.getPlannedStartDateT().isAfter(todayDateT)) {
        break;
      }
    }

    checkIfPlannedStartDateTimeIsBeforeCurrentDateTime(manufacturingOperation);

    manufacturingOperation.setRealStartDateT(null);
    manufacturingOperation.setRealEndDateT(null);
    manufacturingOperation.setRealDuration(null);
  }

  protected void planWithStrategyAndMachine(
      ManufacturingOperation manufacturingOperation, Machine machine) throws AxelorException {

    LocalDateTime plannedEndDate = manufacturingOperation.getPlannedEndDateT();
    LocalDateTime nextOperationDate =
        manufacturingOperationService.getNextOperationDate(manufacturingOperation);
    LocalDateTime minDate = LocalDateTimeHelper.min(plannedEndDate, nextOperationDate);
    if (machine != null) {
      MachineTimeSlot freeMachineTimeSlot =
          machineService.getFurthestAvailableTimeSlotFrom(
              machine,
              minDate.minusSeconds(
                  manufacturingOperationService.getDuration(manufacturingOperation)),
              minDate,
              manufacturingOperation);
      manufacturingOperation.setPlannedStartDateT(freeMachineTimeSlot.getStartDateT());
      manufacturingOperation.setPlannedEndDateT(freeMachineTimeSlot.getEndDateT());

      Long plannedDuration =
          DurationHelper.getSecondsDuration(
              Duration.between(
                  manufacturingOperation.getPlannedStartDateT(),
                  manufacturingOperation.getPlannedEndDateT()));
      manufacturingOperation.setPlannedDuration(plannedDuration);
    } else {
      manufacturingOperation.setPlannedEndDateT(minDate);
      manufacturingOperation.setPlannedStartDateT(
          manufacturingOperation
              .getPlannedEndDateT()
              .minusSeconds(manufacturingOperationService.getDuration(manufacturingOperation)));

      Long plannedDuration =
          DurationHelper.getSecondsDuration(
              Duration.between(
                  manufacturingOperation.getPlannedStartDateT(),
                  manufacturingOperation.getPlannedEndDateT()));
      manufacturingOperation.setPlannedDuration(plannedDuration);
    }
  }

  protected List<Machine> getEligibleMachines(ManufacturingOperation manufacturingOperation) {

    List<Machine> machines = new ArrayList<>();
    Machine machine = manufacturingOperation.getMachine();
    machines.add(machine);
    ProdProcessLine prodProcessLine = manufacturingOperation.getProdProcessLine();
    if (prodProcessLine != null) {
      machines.addAll(
          Optional.ofNullable(prodProcessLine.getWorkCenterGroup())
              .map(WorkCenterGroup::getWorkCenterSet).stream()
              .flatMap(Collection::stream)
              .map(WorkCenter::getMachine)
              .filter(m -> !machine.getId().equals(m.getId()))
              .collect(Collectors.toList()));
    }
    return machines;
  }
}
