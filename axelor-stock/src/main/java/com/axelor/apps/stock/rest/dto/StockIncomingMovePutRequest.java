package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockIncomingMovePutRequest extends RequestStructure {

  @Min(0)
  @NotNull
  private Long toStockLocationId;

  public StockIncomingMovePutRequest() {}

  public Long getToStockLocationId() {
    return toStockLocationId;
  }

  public void setToStockLocationId(Long toStockLocationId) {
    this.toStockLocationId = toStockLocationId;
  }

  // Transform id to object
  public StockLocation fetchToStockLocation() {
    if (this.toStockLocationId != null) {
      return ObjectFinder.find(StockLocation.class, toStockLocationId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
