package com.axelor.apps.production.service.machine;

import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
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
}
