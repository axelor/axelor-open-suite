package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.utils.api.ObjectFinder;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockInternalMoveStockMoveLinePostRequest {

  @NotNull
  @Min(0)
  private Long productId;

  @NotNull
  @Min(0)
  private Long unitId;

  @NotNull
  @Min(0)
  private BigDecimal realQty;

  @Min(0)
  private Long fromStockLocationId;

  @Min(0)
  private Long toStockLocationId;

  public StockInternalMoveStockMoveLinePostRequest() {}

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getUnitId() {
    return unitId;
  }

  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public void setRealQty(BigDecimal realQty) {
    this.realQty = realQty;
  }

  public Long getFromStockLocationId() {
    return fromStockLocationId;
  }

  public void setFromStockLocationId(Long fromStockLocationId) {
    this.fromStockLocationId = fromStockLocationId;
  }

  public Long getToStockLocationId() {
    return toStockLocationId;
  }

  public void setToStockLocationId(Long toStockLocationId) {
    this.toStockLocationId = toStockLocationId;
  }

  // Transform id to object
  public Product fetchProduct() {
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }

  public Unit fetchUnit() {
    return ObjectFinder.find(Unit.class, unitId, ObjectFinder.NO_VERSION);
  }

  public StockLocation fetchFromStockLocation() {
    if (fromStockLocationId != null) {
      return ObjectFinder.find(StockLocation.class, fromStockLocationId, ObjectFinder.NO_VERSION);
    }
    return null;
  }

  public StockLocation fetchtoStockLocation() {
    if (toStockLocationId != null) {
      return ObjectFinder.find(StockLocation.class, toStockLocationId, ObjectFinder.NO_VERSION);
    }
    return null;
  }
}
