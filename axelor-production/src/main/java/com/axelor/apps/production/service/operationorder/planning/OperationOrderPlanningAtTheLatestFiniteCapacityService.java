package com.axelor.apps.production.service.operationorder.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
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

public class OperationOrderPlanningAtTheLatestFiniteCapacityService
    extends OperationOrderPlanningCommonService {

  protected MachineService machineService;

  @Inject
  protected OperationOrderPlanningAtTheLatestFiniteCapacityService(
      OperationOrderService operationOrderService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepository,
      MachineService machineService) {
    super(operationOrderService, operationOrderStockMoveService, operationOrderRepository);
    this.machineService = machineService;
  }

  @Override
  protected void planWithStrategy(OperationOrder operationOrder) throws AxelorException {
    Machine machine = operationOrder.getMachine();
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

    operationOrder.setRealStartDateT(null);
    operationOrder.setRealEndDateT(null);
    operationOrder.setRealDuration(null);
  }
}
