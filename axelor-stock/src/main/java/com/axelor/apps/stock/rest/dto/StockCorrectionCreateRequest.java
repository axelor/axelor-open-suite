package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.stock.db.StockCorrectionReason;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockCorrectionReasonRepository;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.tool.api.RequestStructure;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockCorrectionCreateRequest implements RequestStructure {

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

  public StockCorrectionCreateRequest() {}

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
  public Product getProduct() {
    return Beans.get(ProductRepository.class).find(this.productId);
  }

  public StockLocation getStockLocation() {
    return Beans.get(StockLocationRepository.class).find(this.stockLocationId);
  }

  public StockCorrectionReason getReason() {
    return Beans.get(StockCorrectionReasonRepository.class).find(this.reasonId);
  }

  public TrackingNumber getTrackingNumber() {
    if (this.trackingNumberId != null) {
      return Beans.get(TrackingNumberRepository.class).find(this.trackingNumberId);
    } else {
      return null;
    }
  }
}
