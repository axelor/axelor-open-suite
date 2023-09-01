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
package com.axelor.apps.production.service.machine;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import java.time.LocalDateTime;

public interface MachineService {

  /**
   * Method that return the closest available dateTime for a operation starting from startDateT and
   * end at endDateT. It take into account the weekly planning, the days event planning and the
   * other operations order of the machine.
   *
   * @param machine
   * @param duration
   * @param operationOrder
   * @return the closest available date
   * @throws AxelorException
   */
  MachineTimeSlot getClosestAvailableTimeSlotFrom(
      Machine machine,
      LocalDateTime startDateT,
      LocalDateTime endDateT,
      OperationOrder operationOrder)
      throws AxelorException;
}
