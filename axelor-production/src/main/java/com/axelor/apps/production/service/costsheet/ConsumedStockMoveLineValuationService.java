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
package com.axelor.apps.production.service.costsheet;

import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;

public interface ConsumedStockMoveLineValuationService {

  /**
   * Values a consumed stock move line at the weighted average price recorded when it was realized.
   * A line without a recorded WAP price (move from a virtual location, data prior to its storage)
   * is valued at its company unit price.
   *
   * @param stockMoveLine a consumed stock move line
   * @return real quantity x WAP price, or real quantity x company unit price if no WAP price was
   *     recorded
   */
  BigDecimal computeConsumedValue(StockMoveLine stockMoveLine);
}
