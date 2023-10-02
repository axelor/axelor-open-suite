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
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.auth.db.User;
import java.util.List;

public interface OperationOrderWorkflowService {

  /**
   * Reset the planned dates from the specified operation order list.
   *
   * @param operationOrderList
   * @return
   */
  List<OperationOrder> resetPlannedDates(List<OperationOrder> operationOrderList);

  /**
   * Plans the given {@link OperationOrder} and sets its planned dates
   *
   * @param operationOrder An operation order
   */
  void plan(OperationOrder operationOrder) throws AxelorException;

  /**
   * Plans the given {@link OperationOrder} and sets its planned dates for successive calls, must be
   * called by order of operation order priority. The order must be ascending if useAsapScheduling
   * is true and descending if not.
   *
   * @param operationOrder
   * @param useAsapScheduling
   * @return
   * @throws AxelorException
   */
  void plan(OperationOrder operationOrder, boolean useAsapScheduling) throws AxelorException;

  /**
   * re-plans the given {@link OperationOrder} and sets its planned dates
   *
   * @param operationOrder An operation order
   */
  void replan(OperationOrder operationOrder) throws AxelorException;

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
   * @throws AxelorException
   */
  void pause(OperationOrder operationOrder) throws AxelorException;

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
   * @throws AxelorException
   */
  void stopOperationOrderDuration(OperationOrder operationOrder) throws AxelorException;

  boolean canStartOperationOrder(OperationOrder operationOrder);

  /**
   * Mostly work the same as pause method. Except this one will not set operation order status to
   * standBy if somes operations are still being working on. But, it will surely pause the
   * operationOrderDuration of the user.
   *
   * @param operationOrder
   * @throws AxelorException
   */
  void pause(OperationOrder operationOrder, User user) throws AxelorException;

  /**
   * Ends the last {@link OperationOrderDuration} started by User and sets the real duration of
   * {@code operationOrder}<br>
   * Adds the real duration to the {@link Machine} linked to {@code operationOrder}
   *
   * @param operationOrder An operation order @Param user
   * @throws AxelorException
   */
  void stopOperationOrderDuration(OperationOrder operationOrder, User user) throws AxelorException;

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

  void stopOperationOrderDuration(OperationOrderDuration duration) throws AxelorException;
}
