package com.axelor.apps.production.service.machine;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import com.axelor.utils.date.DurationTool;
import com.google.inject.Inject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class MachineServiceImpl implements MachineService {

  protected OperationOrderRepository operationOrderRepository;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public MachineServiceImpl(OperationOrderRepository operationOrderRepository,
		  WeeklyPlanningService weeklyPlanningService) {
    this.operationOrderRepository = operationOrderRepository;
    this.weeklyPlanningService = weeklyPlanningService;
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
                "self.machine = :machine AND self.plannedStartDateT <= :startDate AND self.plannedEndDateT > :startDate"
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
      return getClosestAvailableDateFrom(
          machine, concurrentOperationOrders.get(0).getPlannedEndDateT(), operationOrder);
    }
  }
  
  @Override
  public MachineTimeSlot getClosestAvailableTimeSlotFrom(
      Machine machine, LocalDateTime startDateT, LocalDateTime endDateT, OperationOrder operationOrder) {
	  
	  return getClosestAvailableTimeSlotFrom(machine, startDateT, endDateT, operationOrder, DurationTool.getSecondsDuration(Duration.between(startDateT, endDateT)));
  }
  
  
  protected MachineTimeSlot getClosestAvailableTimeSlotFrom(
      Machine machine, LocalDateTime startDateT, LocalDateTime endDateT, OperationOrder operationOrder, long initialDuration) {
	  
	    EventsPlanning planning = machine.getPublicHolidayEventsPlanning();
	    // If startDate is not available because of planning
	    // Then we try for the next day
	    if (planning != null
	        && planning.getEventsPlanningLineList() != null
	        && planning.getEventsPlanningLineList().stream()
	            .anyMatch(epl -> epl.getDate().equals(startDateT.toLocalDate()))) {
	      LocalDateTime nextDayDateT = startDateT.plusDays(1).withHour(0).withMinute(0).withSecond(0);
		return getClosestAvailableTimeSlotFrom(
	          machine, nextDayDateT, nextDayDateT.plusSeconds(initialDuration), operationOrder, initialDuration);
	    }
	    
	    if (machine.getWeeklyPlanning() != null) {
	    	//Planning on date at startDateT
	    	DayPlanning dayPlanning = weeklyPlanningService.findDayPlanning(machine.getWeeklyPlanning(), startDateT.toLocalDate());
	    	//If startDateT not in any period of dayPlanning then recursive call with startDateT = most apprioriate startPeriodDate
	    	//If startDateT is after the 2 period of the dayPlanning then recursive call for next day (at 00:00:00).
	    }
	    
	    // The first one of the list will be the last to finish
	    List<OperationOrder> concurrentOperationOrders =
	        operationOrderRepository
	            .all()
	            .filter(
	                "self.machine = :machine"
	                	+ " AND ((self.plannedStartDateT <= :startDate AND self.plannedStartDateT > :endDate)"
	                	+ " OR (self.plannedEndDateT < :startDate AND self.plannedEndDateT > :endDate))"
	                    + " AND (self.manufOrder.statusSelect != :cancelled OR self.manufOrder.statusSelect != :finished)"
	                    + " AND self.id != :operationOrderId")
	            .bind("startDate", startDateT)
	            .bind("endDate", endDateT)
	            .bind("machine", machine)
	            .bind("cancelled", ManufOrderRepository.STATUS_CANCELED)
	            .bind("finished", ManufOrderRepository.STATUS_FINISHED)
	            .bind("operationOrderId", operationOrder.getId())
	            .order("-plannedEndDateT")
	            .fetch();

	    if (concurrentOperationOrders.isEmpty()) {
	    	MachineTimeSlot timeSlot = new MachineTimeSlot(startDateT, startDateT);
	      return timeSlot;
	    } else {
	      return getClosestAvailableTimeSlotFrom(
	          machine, concurrentOperationOrders.get(0).getPlannedEndDateT(), concurrentOperationOrders.get(0).getPlannedEndDateT().plusSeconds(initialDuration), operationOrder, initialDuration);
	    } 
	  
  }
 
}
