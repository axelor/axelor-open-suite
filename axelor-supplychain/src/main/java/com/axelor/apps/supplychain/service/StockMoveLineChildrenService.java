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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.util.List;

public interface StockMoveLineChildrenService {

  /**
   * Apply the same ratio to all descendants in memory (no DB write). Grandchildren are fetched from
   * DB read-only and attached to their parent before the ratio is applied.
   *
   * @param children direct children from the view context
   * @param ratio newRealQty / oldRealQty of the parent
   * @return the updated children list
   */
  List<StockMoveLine> applyRatioToChildren(List<StockMoveLine> children, BigDecimal ratio);

  /**
   * Propagate realQty from parent lines to child lines using only in-memory context data. For each
   * child line (parentStockMoveLine != null), computes: child.realQty = parent.realQty * child.qty
   * / parent.qty
   *
   * @param stockMoveLineList the full list of lines from the StockMove context
   * @return the updated list
   */
  List<StockMoveLine> propagateRealQtyInList(List<StockMoveLine> stockMoveLineList);
}
