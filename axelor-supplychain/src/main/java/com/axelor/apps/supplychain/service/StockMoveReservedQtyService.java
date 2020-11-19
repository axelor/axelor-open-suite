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

import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;

public interface StockMoveReservedQtyService {

  /**
   * Try to allocate every line, meaning the allocated quantity of the line will be changed to match
   * the requested quantity. Ignore line with real qty at 0.
   *
   * @param stockMove a planned stock move.
   * @throws AxelorException if the sale order does not have a stock move.
   */
  void allocateAll(StockMove stockMove) throws AxelorException;
}
