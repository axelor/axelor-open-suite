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
