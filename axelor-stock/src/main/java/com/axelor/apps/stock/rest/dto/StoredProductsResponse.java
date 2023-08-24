package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;

public class StoredProductsResponse extends ResponseStructure {

  private final Long id;
  private final Long storedProductId;
  private final Long trackingNumberId;
  private final Long toStockLocationId;
  private final Long unitId;
  private final BigDecimal storedQty;
  private final BigDecimal currentQty;
  private final Long stockMoveLineId;

  public StoredProductsResponse(StoredProducts storedProducts) {
    super(
        storedProducts.getVersion() != null
            ? storedProducts.getVersion()
            : ObjectFinder.NO_VERSION);
    this.id = storedProducts.getId();
    this.storedProductId = storedProducts.getStoredProduct().getId();
    this.trackingNumberId =
        storedProducts.getTrackingNumber() != null
            ? storedProducts.getTrackingNumber().getId()
            : null;
    this.toStockLocationId =
        storedProducts.getToStockLocation() != null
            ? storedProducts.getToStockLocation().getId()
            : null;
    this.unitId = storedProducts.getStoredProduct().getUnit().getId();
    this.storedQty = storedProducts.getStoredQty();
    this.currentQty = storedProducts.getCurrentQty();
    this.stockMoveLineId =
        storedProducts.getStockMoveLine() != null
            ? storedProducts.getStockMoveLine().getId()
            : null;
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
