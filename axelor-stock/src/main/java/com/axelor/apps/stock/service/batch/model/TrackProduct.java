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
