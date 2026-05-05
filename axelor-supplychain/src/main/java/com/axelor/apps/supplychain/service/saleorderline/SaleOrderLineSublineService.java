/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import java.util.List;
import java.util.Map;

public interface SaleOrderLineSublineService {

  /**
   * Recursively collect all sale order lines in depth-first order, sorted by sequence at each
   * level.
   *
   * @param saleOrderLines root-level lines
   * @return flattened list including all descendants
   */
  List<SaleOrderLine> collectAllLinesRecursively(List<SaleOrderLine> saleOrderLines);

  /**
   * Return true if the line has at least one descendant with managedInStockMove = true.
   *
   * @param saleOrderLine the line to test
   */
  boolean hasAnyManagedChild(SaleOrderLine saleOrderLine);

  /**
   * Set parentStockMoveLine on each StockMoveLine whose SaleOrderLine has a parent, using the
   * provided mapping.
   *
   * @param stockMove the stock move containing the lines
   * @param saleOrderLineToStockMoveLine mapping from SaleOrderLine id to its StockMoveLine
   */
  void linkParentStockMoveLines(
      StockMove stockMove, Map<Long, StockMoveLine> saleOrderLineToStockMoveLine);
}
