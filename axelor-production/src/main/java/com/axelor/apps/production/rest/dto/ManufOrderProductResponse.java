package com.axelor.apps.production.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.tool.api.ResponseStructure;
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

  public ManufOrderProductResponse(
      Product product,
      StockMoveLine stockMoveLine,
      BigDecimal plannedQty,
      BigDecimal realQty,
      BigDecimal missingQty,
      BigDecimal availableStock,
      TrackingNumber trackingNumber,
      Unit unit) {
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
}
