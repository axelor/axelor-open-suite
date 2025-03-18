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
package com.axelor.apps.purchase.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PurchaseRequestLineRequest extends RequestStructure {

  @Min(0)
  private Long productId;

  private String productTitle;

  @Min(0)
  private Long unitId;

  @Min(0)
  @NotNull
  private BigDecimal quantity;

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getProductTitle() {
    return productTitle;
  }

  public void setProductTitle(String productTitle) {
    this.productTitle = productTitle;
  }

  public Long getUnitId() {
    return unitId;
  }

  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public Product fetchProduct() {
    if (productId == null || productId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }

  public Unit fetchUnit() {
    if (unitId == null || unitId == 0L) {
      return null;
    }
    return ObjectFinder.find(Unit.class, unitId, ObjectFinder.NO_VERSION);
  }
}
