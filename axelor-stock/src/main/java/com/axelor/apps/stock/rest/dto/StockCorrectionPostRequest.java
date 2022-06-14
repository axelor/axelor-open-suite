package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockCorrectionReason;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestPostStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockCorrectionPostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long productId;

  @NotNull
  @Min(0)
  private Long stockLocationId;

  @NotNull
  @Min(0)
  private Long reasonId;

  private Long trackingNumberId;

  @NotNull
  @Min(StockCorrectionRepository.STATUS_DRAFT)
  @Max(StockCorrectionRepository.STATUS_VALIDATED)
  private int status;

  @NotNull
  @Min(0)
  private BigDecimal realQty;

  public StockCorrectionPostRequest() {}

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getStockLocationId() {
    return stockLocationId;
  }

  public void setStockLocationId(Long stockLocationId) {
    this.stockLocationId = stockLocationId;
  }

  public Long getReasonId() {
    return reasonId;
  }

  public void setReasonId(Long reasonId) {
    this.reasonId = reasonId;
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public void setTrackingNumberId(Long trackingNumberId) {
    this.trackingNumberId = trackingNumberId;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public void setRealQty(BigDecimal realQty) {
    this.realQty = realQty;
  }

  // Transform id to object
  public Product fetchProduct() {
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }

  public StockLocation fetchStockLocation() {
    return ObjectFinder.find(StockLocation.class, stockLocationId, ObjectFinder.NO_VERSION);
  }

  public StockCorrectionReason fetchReason() {
    return ObjectFinder.find(StockCorrectionReason.class, reasonId, ObjectFinder.NO_VERSION);
  }

  public TrackingNumber fetchTrackingNumber() {
    if (this.trackingNumberId != null) {
      return ObjectFinder.find(TrackingNumber.class, trackingNumberId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
