package com.axelor.apps.production.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.tool.api.ResponseStructure;
import java.math.BigDecimal;

public class ProducedProductResponse extends ResponseStructure {

  private final Long productId;
  private final String productName;
  private final BigDecimal plannedQty;
  private final BigDecimal producedQty;
  private final TrackingNumberResponse trackingNumber;
  private final UnitResponse unit;

  public ProducedProductResponse(
      Product product,
      BigDecimal plannedQty,
      BigDecimal producedQty,
      TrackingNumber trackingNumber,
      Unit unit) {
    super(product.getVersion());
    this.productId = product.getId();
    this.productName = product.getName();
    this.plannedQty = plannedQty;
    this.producedQty = producedQty;
    this.unit = new UnitResponse(unit);
    if (trackingNumber != null) {
      this.trackingNumber = new TrackingNumberResponse(trackingNumber);
    } else {
      this.trackingNumber = null;
    }
  }

  public Long getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public BigDecimal getPlannedQty() {
    return plannedQty;
  }

  public BigDecimal getProducedQty() {
    return producedQty;
  }

  public TrackingNumberResponse getTrackingNumber() {
    return trackingNumber;
  }

  public UnitResponse getUnit() {
    return unit;
  }
}
