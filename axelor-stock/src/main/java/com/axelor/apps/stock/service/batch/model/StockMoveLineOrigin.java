/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service.batch.model;

import com.axelor.apps.stock.db.StockMoveLine;
import java.util.Objects;

public class StockMoveLineOrigin {

  private final StockMoveLine stockMoveLine;
  private final String origin;

  public StockMoveLineOrigin(StockMoveLine stockMoveLine, String origin) {
    this.stockMoveLine = Objects.requireNonNull(stockMoveLine);
    this.origin = origin;
  }

  public StockMoveLine getStockMoveLine() {
    return stockMoveLine;
  }

  public String getOrigin() {
    return origin;
  }

  public StockMoveLineOrigin merge(StockMoveLineOrigin stockMoveLineOriginToBeMerged) {

    this.stockMoveLine.setQty(
        this.stockMoveLine.getQty().add(stockMoveLineOriginToBeMerged.getStockMoveLine().getQty()));
    this.stockMoveLine.setRealQty(
        this.stockMoveLine
            .getRealQty()
            .add(stockMoveLineOriginToBeMerged.getStockMoveLine().getRealQty()));

    return new StockMoveLineOrigin(
        this.stockMoveLine,
        String.format("%s, %s", this.origin, stockMoveLineOriginToBeMerged.getOrigin()));
  }
}
