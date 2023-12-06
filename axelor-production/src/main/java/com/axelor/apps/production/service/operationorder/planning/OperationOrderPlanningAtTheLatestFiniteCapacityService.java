/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.operationorder.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
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

public class OperationOrderPlanningAtTheLatestFiniteCapacityService
    extends OperationOrderPlanningCommonService {

  protected MachineService machineService;

  @Inject
  protected OperationOrderPlanningAtTheLatestFiniteCapacityService(
      OperationOrderService operationOrderService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepository,
      AppBaseService appBaseService,
      MachineService machineService) {
    super(
        operationOrderService,
        operationOrderStockMoveService,
        operationOrderRepository,
        appBaseService);
    this.machineService = machineService;
  }

  @Override
  protected void planWithStrategy(OperationOrder operationOrder) throws AxelorException {

    ManufOrder manufOrder = operationOrder.getManufOrder();
    LocalDateTime todayDateT =
        appBaseService.getTodayDateTime(manufOrder.getCompany()).toLocalDateTime();

    LocalDateTime plannedEndDate = operationOrder.getPlannedEndDateT();
    LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();
    Long plannedDuration = operationOrder.getPlannedDuration();

    List<Machine> machines = getEligibleMachines(operationOrder);
    for (Machine machine : machines) {
      operationOrder.setMachine(machine);
      operationOrder.setPlannedEndDateT(plannedEndDate);
      operationOrder.setPlannedStartDateT(plannedStartDate);
      operationOrder.setPlannedDuration(plannedDuration);
      planWithStrategyAndMachine(operationOrder, machine);

      if (operationOrder.getPlannedStartDateT().isAfter(todayDateT)) {
        break;
      }
    }

    checkIfPlannedStartDateTimeIsBeforeCurrentDateTime(operationOrder);

    operationOrder.setRealStartDateT(null);
    operationOrder.setRealEndDateT(null);
    operationOrder.setRealDuration(null);
  }

  protected void planWithStrategyAndMachine(OperationOrder operationOrder, Machine machine)
      throws AxelorException {

    LocalDateTime plannedEndDate = operationOrder.getPlannedEndDateT();
    LocalDateTime nextOperationDate = operationOrderService.getNextOperationDate(operationOrder);
    LocalDateTime minDate = LocalDateTimeHelper.min(plannedEndDate, nextOperationDate);
    if (machine != null) {
      MachineTimeSlot freeMachineTimeSlot =
          machineService.getFurthestAvailableTimeSlotFrom(
              machine,
              minDate.minusSeconds(operationOrderService.getDuration(operationOrder)),
              minDate,
              operationOrder);
      operationOrder.setPlannedStartDateT(freeMachineTimeSlot.getStartDateT());
      operationOrder.setPlannedEndDateT(freeMachineTimeSlot.getEndDateT());

      Long plannedDuration =
          DurationHelper.getSecondsDuration(
              Duration.between(
                  operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));
      operationOrder.setPlannedDuration(plannedDuration);
    } else {
      operationOrder.setPlannedEndDateT(minDate);
      operationOrder.setPlannedStartDateT(
          operationOrder
              .getPlannedEndDateT()
              .minusSeconds(operationOrderService.getDuration(operationOrder)));

      Long plannedDuration =
          DurationHelper.getSecondsDuration(
              Duration.between(
                  operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));
      operationOrder.setPlannedDuration(plannedDuration);
    }
  }

  protected List<Machine> getEligibleMachines(OperationOrder operationOrder) {

    List<Machine> machines = new ArrayList<>();
    Machine machine = operationOrder.getMachine();
    machines.add(machine);
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
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
