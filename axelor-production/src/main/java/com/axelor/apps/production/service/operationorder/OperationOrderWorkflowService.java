package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrder;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface OperationOrderWorkflowService {

  /**
   * Plan an operation order. For successive calls, must be called by order of operation order
   * priority.
   *
   * @param operationOrder
   * @return
   * @throws AxelorException
   */
  OperationOrder plan(OperationOrder operationOrder, Long cumulatedDuration) throws AxelorException;

  long getMachineSetupDuration(OperationOrder operationOrder) throws AxelorException;

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
   * Reset the planned dates from the specified operation order list.
   *
   * @param operationOrderList
   * @return
   */
  List<OperationOrder> resetPlannedDates(List<OperationOrder> operationOrderList);

  /**
   * Starts the given {@link OperationOrder} and sets its starting time
   *
   * @param operationOrder An operation order
   */
  void start(OperationOrder operationOrder) throws AxelorException;

  /**
   * Pauses the given {@link OperationOrder} and sets its pausing time
   *
   * @param operationOrder An operation order
   */
  void pause(OperationOrder operationOrder);

  /**
   * Resumes the given {@link OperationOrder} and sets its resuming time
   *
   * @param operationOrder An operation order
   */
  void resume(OperationOrder operationOrder);

  /**
   * Ends the given {@link OperationOrder} and sets its stopping time<br>
   * Realizes the linked stock moves
   *
   * @param operationOrder An operation order
   */
  void finish(OperationOrder operationOrder) throws AxelorException;

  void finishAndAllOpFinished(OperationOrder operationOrder) throws AxelorException;

  /**
   * Cancels the given {@link OperationOrder} and its linked stock moves And sets its stopping time
   *
   * @param operationOrder An operation order
   */
  void cancel(OperationOrder operationOrder) throws AxelorException;

  /**
   * Starts an {@link OperationOrderDuration} and links it to the given {@link OperationOrder}
   *
   * @param operationOrder An operation order
   */
  void startOperationOrderDuration(OperationOrder operationOrder);

  /**
   * Ends the last {@link OperationOrderDuration} and sets the real duration of {@code
   * operationOrder}<br>
   * Adds the real duration to the {@link Machine} linked to {@code operationOrder}
   *
   * @param operationOrder An operation order
   */
  void stopOperationOrderDuration(OperationOrder operationOrder);

  /**
   * Compute the duration of operation order, then fill {@link OperationOrder#realDuration} with the
   * computed value.
   *
   * @param operationOrder
   */
  void updateRealDuration(OperationOrder operationOrder);

  /**
   * Computes the duration of all the {@link OperationOrderDuration} of {@code operationOrder}
   *
   * @param operationOrder An operation order
   * @return Real duration of {@code operationOrder}
   */
  Duration computeRealDuration(OperationOrder operationOrder);

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

  long getDuration(OperationOrder operationOrder) throws AxelorException;

  long computeEntireCycleDuration(OperationOrder operationOrder, BigDecimal qty)
      throws AxelorException;
}
