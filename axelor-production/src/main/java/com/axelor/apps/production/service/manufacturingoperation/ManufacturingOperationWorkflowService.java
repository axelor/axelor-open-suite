/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.manufacturingoperation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ManufacturingOperationDuration;
import com.axelor.auth.db.User;
import java.util.List;

public interface ManufacturingOperationWorkflowService {

  /**
   * Reset the planned dates from the specified operation order list.
   *
   * @param manufacturingOperationList
   * @return
   */
  List<ManufacturingOperation> resetPlannedDates(
      List<ManufacturingOperation> manufacturingOperationList);

  /**
   * Plans the given {@link ManufacturingOperation} and sets its planned dates
   *
   * @param manufacturingOperation An operation order
   */
  void plan(ManufacturingOperation manufacturingOperation) throws AxelorException;

  /**
   * re-plans the given {@link ManufacturingOperation} and sets its planned dates
   *
   * @param manufacturingOperation An operation order
   */
  void replan(ManufacturingOperation manufacturingOperation) throws AxelorException;

  /**
   * Starts the given {@link ManufacturingOperation} and sets its starting time
   *
   * @param manufacturingOperation An operation order
   */
  void start(ManufacturingOperation manufacturingOperation) throws AxelorException;

  /**
   * Pauses the given {@link ManufacturingOperation} and sets its pausing time
   *
   * @param manufacturingOperation An operation order
   * @throws AxelorException
   */
  void pause(ManufacturingOperation manufacturingOperation) throws AxelorException;

  /**
   * Resumes the given {@link ManufacturingOperation} and sets its resuming time
   *
   * @param manufacturingOperation An operation order
   */
  void resume(ManufacturingOperation manufacturingOperation);

  /**
   * Ends the given {@link ManufacturingOperation} and sets its stopping time<br>
   * Realizes the linked stock moves
   *
   * @param manufacturingOperation An operation order
   */
  void finish(ManufacturingOperation manufacturingOperation) throws AxelorException;

  void finishAndAllOpFinished(ManufacturingOperation manufacturingOperation) throws AxelorException;

  /**
   * Cancels the given {@link ManufacturingOperation} and its linked stock moves And sets its
   * stopping time
   *
   * @param manufacturingOperation An operation order
   */
  void cancel(ManufacturingOperation manufacturingOperation) throws AxelorException;

  /**
   * Starts an {@link ManufacturingOperationDuration} and links it to the given {@link
   * ManufacturingOperation}
   *
   * @param manufacturingOperation An operation order
   */
  void startManufacturingOperationDuration(ManufacturingOperation manufacturingOperation);

  /**
   * Ends the last {@link ManufacturingOperationDuration} and sets the real duration of {@code
   * manufacturingOperation}<br>
   * Adds the real duration to the {@link Machine} linked to {@code manufacturingOperation}
   *
   * @param manufacturingOperation An operation order
   * @throws AxelorException
   */
  void stopManufacturingOperationDuration(ManufacturingOperation manufacturingOperation)
      throws AxelorException;

  boolean canStartManufacturingOperation(ManufacturingOperation manufacturingOperation);

  /**
   * Mostly work the same as pause method. Except this one will not set operation order status to
   * standBy if somes operations are still being working on. But, it will surely pause the
   * manufacturingOperationDuration of the user.
   *
   * @param manufacturingOperation
   * @throws AxelorException
   */
  void pause(ManufacturingOperation manufacturingOperation, User user) throws AxelorException;

  /**
   * Ends the last {@link ManufacturingOperationDuration} started by User and sets the real duration
   * of {@code manufacturingOperation}<br>
   * Adds the real duration to the {@link Machine} linked to {@code manufacturingOperation}
   *
   * @param manufacturingOperation An operation order @Param user
   * @throws AxelorException
   */
  void stopManufacturingOperationDuration(ManufacturingOperation manufacturingOperation, User user)
      throws AxelorException;

  /**
   * Mostly work the same as finish method. But it will only finish (by stopping) operation order
   * duration of user. If then there is no operation order duration in progress, then it will finish
   * the operation order. Else, the status of operation order will not be changed.
   *
   * @param manufacturingOperation
   * @param user
   */
  void finish(ManufacturingOperation manufacturingOperation, User user) throws AxelorException;

  void start(ManufacturingOperation manufacturingOperation, User user) throws AxelorException;

  void stopManufacturingOperationDuration(ManufacturingOperationDuration duration)
      throws AxelorException;
}
