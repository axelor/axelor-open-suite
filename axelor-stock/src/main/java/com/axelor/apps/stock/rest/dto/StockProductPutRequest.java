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

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockProductPutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long stockLocationId;

  @NotNull private String newLocker;

  public StockProductPutRequest() {}

  public Long getStockLocationId() {
    return stockLocationId;
  }

  public void setStockLocationId(Long stockLocationId) {
    this.stockLocationId = stockLocationId;
  }

  public String getNewLocker() {
    return newLocker;
  }

  public void setNewLocker(String newLocker) {
    this.newLocker = newLocker;
  }

  // Transform id to object

  public StockLocation fetchStockLocation() {
    return ObjectFinder.find(StockLocation.class, stockLocationId, ObjectFinder.NO_VERSION);
  }
}
