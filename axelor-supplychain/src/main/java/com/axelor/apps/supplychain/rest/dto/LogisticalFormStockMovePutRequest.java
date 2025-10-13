package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class LogisticalFormStockMovePutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long stockMoveId;

  public Long getStockMoveId() {
    return stockMoveId;
  }

  public void setStockMoveId(Long stockMoveId) {
    this.stockMoveId = stockMoveId;
  }

  public StockMove fetchStockMove() {
    if (stockMoveId == null || stockMoveId == 0L) {
      return null;
    }
    return ObjectFinder.find(StockMove.class, stockMoveId, ObjectFinder.NO_VERSION);
  }
}
