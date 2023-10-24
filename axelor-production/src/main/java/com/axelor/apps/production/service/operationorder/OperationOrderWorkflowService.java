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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.auth.db.User;
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

  /**
   * Mostly work the same as pause method. Except this one will not set operation order status to
   * standBy if somes operations are still being working on. But, it will surely pause the
   * operationOrderDuration of the user.
   *
   * @param operationOrder
   */
  void pause(OperationOrder operationOrder, User user);

  /**
   * Ends the last {@link OperationOrderDuration} started by User and sets the real duration of
   * {@code operationOrder}<br>
   * Adds the real duration to the {@link Machine} linked to {@code operationOrder}
   *
   * @param operationOrder An operation order @Param user
   */
  void stopOperationOrderDuration(OperationOrder operationOrder, User user);

  /**
   * Mostly work the same as finish method. But it will only finish (by stopping) operation order
   * duration of user. If then there is no operation order duration in progress, then it will finish
   * the operation order. Else, the status of operation order will not be changed.
   *
   * @param operationOrder
   * @param user
   */
  void finish(OperationOrder operationOrder, User user) throws AxelorException;

  void start(OperationOrder operationOrder, User user) throws AxelorException;

  void stopOperationOrderDuration(OperationOrderDuration duration);
}
