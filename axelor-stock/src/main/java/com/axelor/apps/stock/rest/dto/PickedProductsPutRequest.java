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

public class PickedProductsPutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long massMoveId;

  @NotNull
  @Min(0)
  private Long pickedProductId;

  @Min(0)
  private Long trackingNumberId;

  @NotNull
  @Min(0)
  private Long fromStockLocationId;

  @Min(0)
  private Long stockMoveLineId;

  @Min(0)
  private BigDecimal pickedQty;

  @Min(0)
  private BigDecimal currentQty;

  public BigDecimal getCurrentQty() {
    return currentQty;
  }

  public void setCurrentQty(BigDecimal currentQty) {
    this.currentQty = currentQty;
  }

  public Long getMassMoveId() {
    return massMoveId;
  }

  public void setMassMoveId(Long massMoveId) {
    this.massMoveId = massMoveId;
  }

  public Long getPickedProductId() {
    return pickedProductId;
  }

  public void setPickedProductId(Long pickedProductId) {
    this.pickedProductId = pickedProductId;
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public void setTrackingNumberId(Long trackingNumberId) {
    this.trackingNumberId = trackingNumberId;
  }

  public Long getFromStockLocationId() {
    return fromStockLocationId;
  }

  public void setFromStockLocationId(Long fromStockLocationId) {
    this.fromStockLocationId = fromStockLocationId;
  }

  public Long getStockMoveLineId() {
    return stockMoveLineId;
  }

  public void setStockMoveLineId(Long stockMoveLineId) {
    this.stockMoveLineId = stockMoveLineId;
  }

  public BigDecimal getPickedQty() {
    return pickedQty;
  }

  public void setPickedQty(BigDecimal pickedQty) {
    this.pickedQty = pickedQty;
  }

  // Transform id to object
  public MassMove fetchMassMove() {
    return massMoveId != null
        ? ObjectFinder.find(MassMove.class, massMoveId, ObjectFinder.NO_VERSION)
        : null;
  }

  public Product fetchProduct() {
    return pickedProductId != null
        ? ObjectFinder.find(Product.class, pickedProductId, ObjectFinder.NO_VERSION)
        : null;
  }

  public TrackingNumber fetchTrackingNumber() {
    return trackingNumberId != null
        ? ObjectFinder.find(TrackingNumber.class, trackingNumberId, ObjectFinder.NO_VERSION)
        : null;
  }

  public StockLocation fetchFromStockLocation() {
    return fromStockLocationId != null
        ? ObjectFinder.find(StockLocation.class, fromStockLocationId, ObjectFinder.NO_VERSION)
        : null;
  }

  public StockMoveLine fetchstockMoveLine() {
    return stockMoveLineId != null
        ? ObjectFinder.find(StockMoveLine.class, stockMoveLineId, ObjectFinder.NO_VERSION)
        : null;
  }
}
