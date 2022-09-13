package com.axelor.apps.production.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.tool.api.ResponseStructure;
import java.math.BigDecimal;

public class ConsumedProductResponse extends ResponseStructure {

  private final Long productId;
  private final String productName;
  private final BigDecimal plannedQty;
  private final BigDecimal consumedQty;
  private final BigDecimal missingQty;
  private final BigDecimal availableStock;
  private final TrackingNumberResponse trackingNumber;
  private final UnitResponse unit;

  public ConsumedProductResponse(
      Product product,
      BigDecimal plannedQty,
      BigDecimal consumedQty,
      BigDecimal missingQty,
      BigDecimal availableStock,
      Unit unit,
      TrackingNumber trackingNumber) {
    super(product.getVersion());
    this.productId = product.getId();
    this.productName = product.getName();
    this.plannedQty = plannedQty;
    this.consumedQty = consumedQty;
    this.missingQty = missingQty;
    this.availableStock = availableStock;
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

  public BigDecimal getConsumedQty() {
    return consumedQty;
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
}
