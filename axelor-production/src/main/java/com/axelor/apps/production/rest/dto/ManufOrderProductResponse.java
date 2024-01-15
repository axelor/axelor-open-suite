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
package com.axelor.apps.production.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.utils.api.ResponseStructure;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

public class ManufOrderProductResponse extends ResponseStructure {

  private final Long productId;
  private final String productName;
  private final Long stockMoveLineId;
  private final int stockMoveLineVersion;
  private final BigDecimal plannedQty;
  private final BigDecimal realQty;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final BigDecimal missingQty;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final BigDecimal availableStock;

  private final TrackingNumberResponse trackingNumber;
  private final UnitResponse unit;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final Long subManufOrderId;

  public ManufOrderProductResponse(
      Product product,
      StockMoveLine stockMoveLine,
      BigDecimal plannedQty,
      BigDecimal realQty,
      BigDecimal missingQty,
      BigDecimal availableStock,
      TrackingNumber trackingNumber,
      Unit unit,
      ManufOrder subManufOrder) {
    super(product.getVersion());
    this.productId = product.getId();
    this.productName = product.getName();
    this.stockMoveLineId = stockMoveLine.getId();
    this.stockMoveLineVersion = stockMoveLine.getVersion();
    this.plannedQty = plannedQty;
    this.realQty = realQty;
    this.missingQty = missingQty;
    this.availableStock = availableStock;
    this.unit = new UnitResponse(unit);
    if (trackingNumber != null) {
      this.trackingNumber = new TrackingNumberResponse(trackingNumber);
    } else {
      this.trackingNumber = null;
    }
    if (subManufOrder != null) {
      this.subManufOrderId = subManufOrder.getId();
    } else {
      this.subManufOrderId = null;
    }
  }

  public Long getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public Long getStockMoveLineId() {
    return stockMoveLineId;
  }

  public int getStockMoveLineVersion() {
    return stockMoveLineVersion;
  }

  public BigDecimal getPlannedQty() {
    return plannedQty;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public BigDecimal getMissingQty() {
    return missingQty;
  }

  public BigDecimal getAvailableStock() {
    return availableStock;
  }

  public TrackingNumberResponse getTrackingNumber() {
    return trackingNumber;
  }

  public UnitResponse getUnit() {
    return unit;
  }

  public Long getSubManufOrderId() {
    return subManufOrderId;
  }
}
