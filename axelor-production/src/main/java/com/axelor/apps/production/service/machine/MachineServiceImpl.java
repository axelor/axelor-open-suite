package com.axelor.apps.production.service.machine;

import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.google.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

public class MachineServiceImpl implements MachineService {

  protected OperationOrderRepository operationOrderRepository;

  @Inject
  public MachineServiceImpl(OperationOrderRepository operationOrderRepository) {
    this.operationOrderRepository = operationOrderRepository;
  }

  @Override
  public LocalDateTime getClosestAvailableDateFrom(
      Machine machine, LocalDateTime startDate, OperationOrder operationOrder) {

    EventsPlanning planning = machine.getPublicHolidayEventsPlanning();
    // If startDate is not available because of planning
    // Then we try for the next day
    if (planning != null
        && planning.getEventsPlanningLineList() != null
        && planning.getEventsPlanningLineList().stream()
            .anyMatch(epl -> epl.getDate().equals(startDate.toLocalDate()))) {
      return getClosestAvailableDateFrom(
          machine, startDate.plusDays(1).withHour(0).withMinute(0).withSecond(0), operationOrder);
    }
    // The first one of the list will be the last to finish
    List<OperationOrder> concurrentOperationOrders =
        operationOrderRepository
            .all()
            .filter(
                "self.machine = :machine AND self.plannedStartDateT <= :startDate AND self.plannedEndDateT >= :startDate"
                    + " AND (self.manufOrder.statusSelect != :cancelled OR self.manufOrder.statusSelect != :finished)"
                    + " AND self.id != :operationOrderId")
            .bind("startDate", startDate)
            .bind("machine", machine)
            .bind("cancelled", ManufOrderRepository.STATUS_CANCELED)
            .bind("finished", ManufOrderRepository.STATUS_FINISHED)
            .bind("operationOrderId", operationOrder.getId())
            .order("-plannedEndDateT")
            .fetch();

    if (concurrentOperationOrders.isEmpty()) {
      return startDate;
    } else {
      return concurrentOperationOrders.get(0).getPlannedEndDateT();
    }
  }
}
