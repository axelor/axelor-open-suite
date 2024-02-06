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
import com.axelor.apps.production.db.ManufacturingOperation;
import java.time.LocalDateTime;
import java.util.List;

public interface ManufacturingOperationPlanningService {

  /**
   * Plan a list of operation orders
   *
   * @param manufacturingOperations
   * @throws AxelorException
   */
  void plan(List<ManufacturingOperation> manufacturingOperations) throws AxelorException;

  /**
   * Re-plan a list of operation orders
   *
   * @param manufacturingOperations
   * @throws AxelorException
   */
  void replan(List<ManufacturingOperation> manufacturingOperations) throws AxelorException;

  /**
   * Set planned start and end dates.
   *
   * @param manufacturingOperation
   * @param plannedStartDateT
   * @param plannedEndDateT
   * @return
   * @throws AxelorException
   */
  ManufacturingOperation setPlannedDates(
      ManufacturingOperation manufacturingOperation,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT)
      throws AxelorException;

  /**
   * Set real start and end dates.
   *
   * @param manufacturingOperation
   * @param realStartDateT
   * @param realEndDateT
   * @return
   * @throws AxelorException
   */
  ManufacturingOperation setRealDates(
      ManufacturingOperation manufacturingOperation,
      LocalDateTime realStartDateT,
      LocalDateTime realEndDateT)
      throws AxelorException;

  boolean willPlannedEndDateOverflow(ManufacturingOperation manufacturingOperation)
      throws AxelorException;

  ManufacturingOperation computeDuration(ManufacturingOperation manufacturingOperation);

  /**
   * Compute the duration of operation order, then fill {@link ManufacturingOperation#realDuration}
   * with the computed value.
   *
   * @param manufacturingOperation
   */
  void updateRealDuration(ManufacturingOperation manufacturingOperation);
}
