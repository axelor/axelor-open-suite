package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;

public class StoredProductResponse extends ResponseStructure {

  private final Long id;
  private final Long storedProductId;
  private final Long trackingNumberId;
  private final Long toStockLocationId;
  private final Long unitId;
  private final BigDecimal storedQty;
  private final BigDecimal currentQty;
  private final Long stockMoveLineId;

  public StoredProductResponse(StoredProduct storedProduct) {
    super(
        storedProduct.getVersion() != null ? storedProduct.getVersion() : ObjectFinder.NO_VERSION);
    this.id = storedProduct.getId();
    this.storedProductId = storedProduct.getStoredProduct().getId();
    this.trackingNumberId =
        storedProduct.getTrackingNumber() != null
            ? storedProduct.getTrackingNumber().getId()
            : null;
    this.toStockLocationId =
        storedProduct.getToStockLocation() != null
            ? storedProduct.getToStockLocation().getId()
            : null;
    this.unitId = storedProduct.getStoredProduct().getUnit().getId();
    this.storedQty = storedProduct.getStoredQty();
    this.currentQty = storedProduct.getCurrentQty();
    this.stockMoveLineId =
        storedProduct.getStockMoveLine() != null ? storedProduct.getStockMoveLine().getId() : null;
  }

  public long getId() {
    return id;
  }

  public Long getStoredProductId() {
    return storedProductId;
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public Long getToStockLocationId() {
    return toStockLocationId;
  }

  public Long getUnitId() {
    return unitId;
  }

  public BigDecimal getStoredQty() {
    return storedQty;
  }

  public BigDecimal getCurrentQty() {
    return currentQty;
  }

  public Long getStockMoveLineId() {
    return stockMoveLineId;
  }
}
