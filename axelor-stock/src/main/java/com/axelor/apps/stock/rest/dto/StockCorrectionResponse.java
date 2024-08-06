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
package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.utils.api.ResponseStructure;

public class StockCorrectionResponse extends ResponseStructure {

  private final Long id;
  private final Long productId;
  private final int realQty;

  public StockCorrectionResponse(StockCorrection stockCorrection) {
    super(stockCorrection.getVersion());
    this.id = stockCorrection.getId();
    this.productId = stockCorrection.getProduct().getId();
    this.realQty = stockCorrection.getRealQty().intValue();
  }

  public Long getId() {
    return id;
  }

  public Long getProductId() {
    return productId;
  }

  public int getRealQty() {
    return realQty;
  }
}
