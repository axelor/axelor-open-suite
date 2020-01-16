/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.exception.AxelorException;

public interface StockLocationLineReservationService {

  /**
   * If the requested quantity is greater than the allocated quantity, will allocate the requested
   * quantity in requesting stock move lines.
   *
   * @param stockLocationLine
   */
  void allocateAll(StockLocationLine stockLocationLine) throws AxelorException;

  /**
   * For every stock move lines, put reserved quantity at 0 without changing requested quantity.
   *
   * @param stockLocationLine
   * @throws AxelorException
   */
  void deallocateAll(StockLocationLine stockLocationLine) throws AxelorException;
}
