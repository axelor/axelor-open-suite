package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PackagingLinePostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long packagingId;

  @Min(0)
  private Long stockMoveLineId;

  @Min(0)
  private BigDecimal quantity;

  public Long getPackagingId() {
    return packagingId;
  }

  public void setPackagingId(Long packagingId) {
    this.packagingId = packagingId;
  }

  public Long getStockMoveLineId() {
    return stockMoveLineId;
  }

  public void setStockMoveLineId(Long stockMoveLineId) {
    this.stockMoveLineId = stockMoveLineId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public Packaging fetchPackaging() {
    if (packagingId == null || packagingId == 0L) {
      return null;
    }
    return ObjectFinder.find(Packaging.class, packagingId, ObjectFinder.NO_VERSION);
  }

  public StockMoveLine fetchStockMoveLine() {
    if (stockMoveLineId == null || stockMoveLineId == 0L) {
      return null;
    }
    return ObjectFinder.find(StockMoveLine.class, stockMoveLineId, ObjectFinder.NO_VERSION);
  }
}
