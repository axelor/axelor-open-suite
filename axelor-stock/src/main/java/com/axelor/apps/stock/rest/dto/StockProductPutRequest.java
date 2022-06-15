package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockProductPutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long stockLocationId;

  @NotNull private String newLocker;

  public StockProductPutRequest() {}

  public Long getStockLocationId() {
    return stockLocationId;
  }

  public void setStockLocationId(Long stockLocationId) {
    this.stockLocationId = stockLocationId;
  }

  public String getNewLocker() {
    return newLocker;
  }

  public void setNewLocker(String newLocker) {
    this.newLocker = newLocker;
  }

  // Transform id to object

  public StockLocation fetchStockLocation() {
    return ObjectFinder.find(StockLocation.class, stockLocationId, ObjectFinder.NO_VERSION);
  }
}
