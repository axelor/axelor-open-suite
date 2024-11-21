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
package com.axelor.apps.stock.service.batch.model;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.TrackingNumber;
import java.util.Objects;

public class TrackProduct {

  private final Product product;
  private final TrackingNumber trackingNumber;

  public TrackProduct(Product product, TrackingNumber trackingNumber) {
    this.product = Objects.requireNonNull(product);
    this.trackingNumber = trackingNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TrackProduct)) {
      return false;
    }
    TrackProduct trackProduct = (TrackProduct) obj;
    if (this.trackingNumber == null && trackProduct.trackingNumber == null) {
      return this.product.equals(trackProduct.product);
    }
    if (this.trackingNumber != null) {
      return this.product.equals(trackProduct.product)
          && this.trackingNumber.equals(trackProduct.trackingNumber);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = this.product.getName().hashCode();
    if (this.trackingNumber != null) {
      hash += this.trackingNumber.getTrackingNumberSeq().hashCode();
    }
    return hash;
  }
}
