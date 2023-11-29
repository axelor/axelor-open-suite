package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;

public class PickedProductResponse extends ResponseStructure {

  private final Long id;
  private final Long pickedProductId;
  private final Long trackingNumberId;
  private final Long fromStockLocationId;
  private final Long unitId;
  private final BigDecimal pickedQty;
  private final BigDecimal currentQty;
  private final Long stockMoveLineId;

  public PickedProductResponse(PickedProduct pickedProduct) {
    super(
        pickedProduct.getVersion() != null ? pickedProduct.getVersion() : ObjectFinder.NO_VERSION);
    this.id = pickedProduct.getId();
    this.pickedProductId = pickedProduct.getPickedProduct().getId();
    this.trackingNumberId =
        pickedProduct.getTrackingNumber() != null
            ? pickedProduct.getTrackingNumber().getId()
            : null;
    this.fromStockLocationId =
        pickedProduct.getFromStockLocation() != null
            ? pickedProduct.getFromStockLocation().getId()
            : null;
    this.unitId = pickedProduct.getPickedProduct().getUnit().getId();
    this.pickedQty = pickedProduct.getPickedQty();
    this.currentQty = pickedProduct.getCurrentQty();
    this.stockMoveLineId =
        pickedProduct.getStockMoveLine() != null ? pickedProduct.getStockMoveLine().getId() : null;
  }

  public Long getId() {
    return id;
  }

  public Long getPickedProductId() {
    return pickedProductId;
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public Long getFromStockLocationId() {
    return fromStockLocationId;
  }

  public Long getUnitId() {
    return unitId;
  }

  public BigDecimal getPickedQty() {
    return pickedQty;
  }

  public BigDecimal getCurrentQty() {
    return currentQty;
  }

  public Long getStockMoveLineId() {
    return stockMoveLineId;
  }
}
