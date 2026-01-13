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
package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PackagingLinePostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long packagingId;

  @NotNull
  @Min(0)
  private Long stockMoveLineId;

  @Min(0)
  private BigDecimal quantity;

  public Long getPackagingId() {
    return packagingId;
  }

  public void setPackagingId(Long packagingId) {
    this.packagingId = packagingId;
  }

  public Long getStockMoveLineId() {
    return stockMoveLineId;
  }

  public void setStockMoveLineId(Long stockMoveLineId) {
    this.stockMoveLineId = stockMoveLineId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public Packaging fetchPackaging() {
    if (packagingId == null || packagingId == 0L) {
      return null;
    }
    return ObjectFinder.find(Packaging.class, packagingId, ObjectFinder.NO_VERSION);
  }

  public StockMoveLine fetchStockMoveLine() {
    if (stockMoveLineId == null || stockMoveLineId == 0L) {
      return null;
    }
    return ObjectFinder.find(StockMoveLine.class, stockMoveLineId, ObjectFinder.NO_VERSION);
  }
}
