package com.axelor.apps.production.service.operationorder.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.DurationHelper;
import com.axelor.utils.helpers.date.LocalDateTimeHelper;
import com.google.inject.Inject;
import java.math.RoundingMode;
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
  protected AppBaseService appBaseService;

  @Inject
  protected OperationOrderPlanningAtTheLatestFiniteCapacityService(
      OperationOrderService operationOrderService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepository,
      MachineService machineService,
      AppBaseService appBaseService) {
    super(operationOrderService, operationOrderStockMoveService, operationOrderRepository);
    this.machineService = machineService;
    this.appBaseService = appBaseService;
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

    if (operationOrder.getPlannedStartDateT().isBefore(todayDateT)) {
      int qtyScale = appBaseService.getNbDecimalDigitForQty();
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PLAN_IS_BEFORE_TODAY_DATE),
          String.format(
              "%s %s",
              manufOrder.getQty() != null
                  ? manufOrder.getQty().setScale(qtyScale, RoundingMode.HALF_UP)
                  : null,
              manufOrder.getProduct().getFullName()));
    }

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

  List<Machine> getEligibleMachines(OperationOrder operationOrder) {

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
