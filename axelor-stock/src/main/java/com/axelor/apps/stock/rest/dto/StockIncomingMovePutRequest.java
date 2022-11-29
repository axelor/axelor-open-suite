/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockIncomingMovePutRequest extends RequestStructure {

  @Min(0)
  @NotNull
  private Long toStockLocationId;

  public StockIncomingMovePutRequest() {}

  public Long getToStockLocationId() {
    return toStockLocationId;
  }

  public void setToStockLocationId(Long toStockLocationId) {
    this.toStockLocationId = toStockLocationId;
  }

  // Transform id to object
  public StockLocation fetchToStockLocation() {
    if (this.toStockLocationId != null) {
      return ObjectFinder.find(StockLocation.class, toStockLocationId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
