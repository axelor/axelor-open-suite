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

  boolean canStartOperationOrder(OperationOrder operationOrder);
}
