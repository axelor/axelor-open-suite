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
package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.tool.api.ResponseStructure;

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
