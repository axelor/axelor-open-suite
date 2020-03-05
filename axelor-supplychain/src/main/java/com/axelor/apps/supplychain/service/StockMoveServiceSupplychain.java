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
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface StockMoveServiceSupplychain {

  public List<StockMoveLine> addSubLines(List<StockMoveLine> list);

  public List<StockMoveLine> removeSubLines(List<StockMoveLine> lines);

  /**
   * For all lines in this stock move with quantity equal to 0, we empty the link to sale order
   * lines, allowing to delete non delivered sale order lines.
   *
   * @param stockMove
   */
  void detachNonDeliveredStockMoveLines(StockMove stockMove);

  void verifyProductStock(StockMove stockMove) throws AxelorException;

  public boolean isAddAllocatedStockMoveLine(StockMove stockMove);
}
