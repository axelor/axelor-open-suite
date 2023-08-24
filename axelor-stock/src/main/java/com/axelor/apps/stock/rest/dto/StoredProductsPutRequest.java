package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassMove;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StoredProductsPutRequest extends RequestStructure {
  @NotNull
  @Min(0)
  private Long massMoveId;

  @NotNull
  @Min(0)
  private Long storedProductId;

  @Min(0)
  private Long trackingNumberId;

  @Min(0)
  private BigDecimal currentQty;

  @Min(0)
  private BigDecimal storedQty;

  @NotNull
  @Min(0)
  private Long toStockLocationId;

  @Min(0)
  private Long stockMoveLineId;

  public Long getMassMoveId() {
    return massMoveId;
  }

  public void setMassMoveId(Long massMoveId) {
    this.massMoveId = massMoveId;
  }

  public Long getStoredProductId() {
    return storedProductId;
  }

  public void setStoredProductId(Long storedProductId) {
    this.storedProductId = storedProductId;
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public void setTrackingNumberId(Long trackingNumberId) {
    this.trackingNumberId = trackingNumberId;
  }

  public BigDecimal getCurrentQty() {
    return currentQty;
  }

  public void setCurrentQty(BigDecimal currentQty) {
    this.currentQty = currentQty;
  }

  public BigDecimal getStoredQty() {
    return storedQty;
  }

  public void setStoredQty(BigDecimal storedQty) {
    this.storedQty = storedQty;
  }

  public Long getToStockLocationId() {
    return toStockLocationId;
  }

  public void setToStockLocationId(Long toStockLocationId) {
    this.toStockLocationId = toStockLocationId;
  }

  public Long getStockMoveLineId() {
    return stockMoveLineId;
  }

  public void setStockMoveLineId(Long stockMoveLineId) {
    this.stockMoveLineId = stockMoveLineId;
  }

  // Transform id to object
  public MassMove fetchMassMove() {
    return ObjectFinder.find(MassMove.class, massMoveId, ObjectFinder.NO_VERSION);
  }

  public Product fetchStoredProduct() {
    return ObjectFinder.find(Product.class, storedProductId, ObjectFinder.NO_VERSION);
  }

  public StockMoveLine fetchStockMoveLine() {
    if (this.stockMoveLineId != null) {
      return ObjectFinder.find(StockMoveLine.class, stockMoveLineId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }

  public StockLocation fetchToStockLocation() {
    if (this.toStockLocationId != null) {
      return ObjectFinder.find(StockLocation.class, toStockLocationId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }

  public TrackingNumber fetchTrackingNumber() {
    if (this.trackingNumberId != null) {
      return ObjectFinder.find(TrackingNumber.class, trackingNumberId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
