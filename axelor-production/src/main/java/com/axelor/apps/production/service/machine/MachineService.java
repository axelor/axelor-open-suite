package com.axelor.apps.production.service.machine;

import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.model.machine.MachineTimeSlot;

import java.time.LocalDateTime;

public interface MachineService {

  /**
   * Method that return the closest available date (starting from startDate) for the machine.
   *
   * @param machine
   * @param maxDate
   * @param operationOrder
   * @return the closest available date
   */
  LocalDateTime getClosestAvailableDateFrom(
      Machine machine, LocalDateTime startDate, OperationOrder operationOrder);

	/**
   * Method that return the closest available dateTime for a operation starting from startDateT and end at endDateT. 
   * It take into account the weekly planning, the days event planning and 
   * the other operations order of the machine. 
   *
   * @param machine
   * @param duration
   * @param operationOrder
   * @return the closest available date
   */
    MachineTimeSlot getClosestAvailableTimeSlotFrom(Machine machine, LocalDateTime startDateT, LocalDateTime endDateT,
			OperationOrder operationOrder);

}
