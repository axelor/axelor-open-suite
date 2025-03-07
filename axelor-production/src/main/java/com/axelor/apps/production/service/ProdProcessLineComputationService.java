/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;

public interface ProdProcessLineComputationService {

  /**
   * Compute and return number of cycle for a given qty
   *
   * @param prodProcessLine
   * @param qty
   * @return Number of cycle.
   */
  BigDecimal getNbCycle(ProdProcessLine prodProcessLine, BigDecimal qty);

  BigDecimal computeNbCycle(BigDecimal qty, BigDecimal maxCapacityPerCycle);

  /**
   * Compute and return the installing duration which is: <br>
   * setup duration * (nbCycle - 1) + starting duration + ending duration
   *
   * @param prodProcessLine
   * @return installing duration
   */
  BigDecimal getMachineInstallingDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException;

  BigDecimal getHourMachineDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException;

  /**
   * Compute the total machine duration for a given nbCycles
   *
   * @param prodProcessLine
   * @param nbCycles
   * @return totalMachineDuration
   */
  BigDecimal getMachineDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException;

  /**
   * Compute the total human duration for a given nbCycles
   *
   * @param prodProcessLine
   * @param nbCycles
   * @return totalMachineDuration
   */
  BigDecimal getHumanDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles);

  /**
   * Compute the total duration for a given nbCycles
   *
   * @param prodProcessLine
   * @param nbCycles
   * @return total duration
   */
  BigDecimal getTotalDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException;

  /**
   * Compute the entire cycle duration of the prod process line with qty given.
   *
   * @param operationOrder
   * @param prodProcessLine
   * @param qty
   * @throws AxelorException
   */
  long computeEntireCycleDuration(
      OperationOrder operationOrder, ProdProcessLine prodProcessLine, BigDecimal qty)
      throws AxelorException;
}
