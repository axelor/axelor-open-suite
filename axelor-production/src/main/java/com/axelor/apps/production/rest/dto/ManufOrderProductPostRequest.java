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
import com.axelor.apps.production.rest.ManufOrderProductRestService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ManufOrderProductPostRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long productId;

  @Min(0)
  private Long trackingNumberId;

  @NotNull
  @Min(0)
  private BigDecimal qty;

  @NotNull
  @Pattern(
      regexp =
          ManufOrderProductRestService.PRODUCT_TYPE_PRODUCED
              + "|"
              + ManufOrderProductRestService.PRODUCT_TYPE_CONSUMED,
      flags = Pattern.Flag.CASE_INSENSITIVE)
  private String productType;

  public ManufOrderProductPostRequest() {};

  public long getProductId() {
    return productId;
  }

  public void setProductId(long productId) {
    this.productId = productId;
  }

  public long getTrackingNumberId() {
    return trackingNumberId;
  }

  public void setTrackingNumberId(long trackingNumberId) {
    this.trackingNumberId = trackingNumberId;
  }

  public BigDecimal getQty() {
    return qty;
  }

  public void setQty(BigDecimal qty) {
    this.qty = qty;
  }

  public String getProductType() {
    return productType;
  }

  public void setProductType(String productType) {
    this.productType = productType;
  }

  public TrackingNumber fetchTrackingNumber() {
    if (trackingNumberId == null) {
      return null;
    }
    return ObjectFinder.find(TrackingNumber.class, trackingNumberId, ObjectFinder.NO_VERSION);
  }

  public Product fetchProduct() {
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }
}
