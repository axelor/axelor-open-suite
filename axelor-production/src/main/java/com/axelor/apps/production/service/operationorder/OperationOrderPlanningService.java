package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrder;
import java.time.LocalDateTime;

public interface OperationOrderPlanningService {

  /**
   * Plan an operation order. For successive calls, must be called by order of operation order
   * priority.
   *
   * @param operationOrder
   * @param cumulatedDuration
   * @return
   * @throws AxelorException
   */
  OperationOrder plan(OperationOrder operationOrder, Long cumulatedDuration) throws AxelorException;

  /**
   * Plan an operation order. For successive calls, must be called by order of operation order
   * priority. The order must be ascending if useAsapScheduling is true and descending if not.
   *
   * @param operationOrder
   * @param cumulatedDuration
   * @param useAsapScheduling
   * @return
   * @throws AxelorException
   */
  OperationOrder plan(
      OperationOrder operationOrder, Long cumulatedDuration, boolean useAsapScheduling)
      throws AxelorException;

  /**
   * Replan an operation order. For successive calls, must reset planned dates first, then call by
   * order of operation order priority.
   *
   * @param operationOrder
   * @return
   * @throws AxelorException
   */
  OperationOrder replan(OperationOrder operationOrder) throws AxelorException;

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

  OperationOrder computeDuration(OperationOrder operationOrder);

  /**
   * Compute the duration of operation order, then fill {@link OperationOrder#realDuration} with the
   * computed value.
   *
   * @param operationOrder
   */
  void updateRealDuration(OperationOrder operationOrder);
}
