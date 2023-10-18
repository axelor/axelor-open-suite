package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrder;
import java.time.LocalDateTime;
import java.util.List;

public interface OperationOrderPlanningService {

  /**
   * Plan a list of operation orders
   *
   * @param operationOrders
   * @throws AxelorException
   */
  void plan(List<OperationOrder> operationOrders) throws AxelorException;

  /**
   * Re-plan a list of operation orders
   *
   * @param operationOrders
   * @throws AxelorException
   */
  void replan(List<OperationOrder> operationOrders) throws AxelorException;

  /**
   * Set planned start and end dates.
   *
   * @param operationOrder
   * @param plannedStartDateT
   * @param plannedEndDateT
   * @return
   * @throws AxelorException
   */
  OperationOrder setPlannedDates(
      OperationOrder operationOrder, LocalDateTime plannedStartDateT, LocalDateTime plannedEndDateT)
      throws AxelorException;

  /**
   * Set real start and end dates.
   *
   * @param operationOrder
   * @param realStartDateT
   * @param realEndDateT
   * @return
   * @throws AxelorException
   */
  OperationOrder setRealDates(
      OperationOrder operationOrder, LocalDateTime realStartDateT, LocalDateTime realEndDateT)
      throws AxelorException;

  boolean willPlannedEndDateOverflow(OperationOrder operationOrder) throws AxelorException;

  OperationOrder computeDuration(OperationOrder operationOrder);

  /**
   * Compute the duration of operation order, then fill {@link OperationOrder#realDuration} with the
   * computed value.
   *
   * @param operationOrder
   */
  void updateRealDuration(OperationOrder operationOrder);
}
