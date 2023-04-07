/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface StockMoveUpdateService {

  /** @deprecated To update status of a stock move (API AOS) */
  @Deprecated
  void updateStatus(StockMove stockMove, Integer status) throws AxelorException;

  /**
   * @deprecated To update unit or qty of an internal stock move with one product, mostly for mobile
   *     app (API AOS)
   */
  @Deprecated
  void updateStockMoveMobility(StockMove stockMove, BigDecimal movedQty, Unit unit)
      throws AxelorException;
}
