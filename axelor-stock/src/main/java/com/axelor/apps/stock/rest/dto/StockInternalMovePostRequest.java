/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockInternalMovePostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long fromStockLocationId;

  @NotNull
  @Min(0)
  private Long toStockLocationId;

  @NotNull
  @Min(0)
  private Long companyId;

  private List<StockInternalMoveStockMoveLinePostRequest> lineList;

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Long getFromStockLocationId() {
    return fromStockLocationId;
  }

  public void setFromStockLocationId(Long fromStockLocationId) {
    this.fromStockLocationId = fromStockLocationId;
  }

  public Long getToStockLocationId() {
    return toStockLocationId;
  }

  public void setToStockLocationId(Long toStockLocationId) {
    this.toStockLocationId = toStockLocationId;
  }

  public List<StockInternalMoveStockMoveLinePostRequest> getLineList() {
    return lineList;
  }

  public void setLineList(List<StockInternalMoveStockMoveLinePostRequest> lineList) {
    this.lineList = lineList;
  }

  public StockLocation fetchFromStockLocation() {
    return ObjectFinder.find(StockLocation.class, fromStockLocationId, ObjectFinder.NO_VERSION);
  }

  public StockLocation fetchToStockLocation() {
    return ObjectFinder.find(StockLocation.class, toStockLocationId, ObjectFinder.NO_VERSION);
  }

  public Company fetchCompany() {
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }
}
