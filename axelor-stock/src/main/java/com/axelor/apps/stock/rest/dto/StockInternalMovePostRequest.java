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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockInternalMovePostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long productId;

  @NotNull
  @Min(0)
  private Long originStockLocationId;

  @NotNull
  @Min(0)
  private Long destStockLocationId;

  @NotNull
  @Min(0)
  private Long companyId;

  @Min(0)
  private Long trackingNumberId;

  @NotNull
  @Min(0)
  private Long unitId;

  @NotNull
  @Min(0)
  private BigDecimal movedQty;

  public StockInternalMovePostRequest() {}

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getOriginStockLocationId() {
    return originStockLocationId;
  }

  public void setOriginStockLocationId(Long originStockLocationId) {
    this.originStockLocationId = originStockLocationId;
  }

  public Long getDestStockLocationId() {
    return destStockLocationId;
  }

  public void setDestStockLocationId(Long destStockLocationId) {
    this.destStockLocationId = destStockLocationId;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public void setTrackingNumberId(Long trackingNumberId) {
    this.trackingNumberId = trackingNumberId;
  }

  public Long getUnitId() {
    return unitId;
  }

  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  public BigDecimal getMovedQty() {
    return movedQty;
  }

  public void setMovedQty(BigDecimal movedQty) {
    this.movedQty = movedQty;
  }

  // Transform id to object
  public Product fetchProduct() {
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }

  public StockLocation fetchOriginStockLocation() {
    return ObjectFinder.find(StockLocation.class, originStockLocationId, ObjectFinder.NO_VERSION);
  }

  public StockLocation fetchDestStockLocation() {
    return ObjectFinder.find(StockLocation.class, destStockLocationId, ObjectFinder.NO_VERSION);
  }

  public Company fetchCompany() {
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }

  public Unit fetchUnit() {
    return ObjectFinder.find(Unit.class, unitId, ObjectFinder.NO_VERSION);
  }

  public TrackingNumber fetchTrackingNumber() {
    if (this.trackingNumberId != null) {
      return ObjectFinder.find(TrackingNumber.class, trackingNumberId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
