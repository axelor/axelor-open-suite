package com.axelor.apps.production.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ManufOrderProductPostRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long productId;

  @Min(0)
  private Long trackingNumberId;

  @NotNull
  @Min(0)
  private BigDecimal qty;

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
