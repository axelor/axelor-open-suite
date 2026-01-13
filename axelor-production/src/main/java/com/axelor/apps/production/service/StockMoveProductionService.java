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
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveService;

public interface StockMoveProductionService extends StockMoveService {

  /**
   * Only call this method when you are currently cancelling a manufacturing order. This is
   * bypassing the check on existing MO to allow the stock move to be cancelled.
   *
   * @param stockMove a stock move linked to a manufacturing order.
   * @throws AxelorException
   */
  void cancelFromManufOrder(StockMove stockMove) throws AxelorException;

  void cancelFromManufOrder(StockMove stockMove, CancelReason cancelReason) throws AxelorException;
}
