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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMoveLine;
import java.util.List;

public interface ManufOrderCheckStockMoveLineService {

  /**
   * Check the realized consumed stock move lines in manuf order has not changed.
   *
   * @param manufOrder a manuf order from context.
   * @param oldManufOrder a manuf order from database.
   * @throws AxelorException if the check fails.
   */
  void checkConsumedStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException;

  /**
   * Check the realized produced stock move lines in manuf order has not changed.
   *
   * @param manufOrder a manuf order from context.
   * @param oldManufOrder a manuf order from database.
   * @throws AxelorException if the check fails.
   */
  void checkProducedStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException;

  void checkResidualStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException;

  /**
   * Check between a new and an old stock move line list whether a realized stock move line has been
   * deleted.
   *
   * @param stockMoveLineList a stock move line list from view context.
   * @param oldStockMoveLineList a stock move line list from database.
   * @throws AxelorException if the check fails.
   */
  void checkRealizedStockMoveLineList(
      List<StockMoveLine> stockMoveLineList, List<StockMoveLine> oldStockMoveLineList)
      throws AxelorException;
}
