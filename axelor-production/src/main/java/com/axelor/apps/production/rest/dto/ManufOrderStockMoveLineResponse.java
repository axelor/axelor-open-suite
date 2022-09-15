package com.axelor.apps.production.rest.dto;

import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.tool.api.ResponseStructure;

public class ManufOrderStockMoveLineResponse extends ResponseStructure {
  private Long stockMoveLineId;

  public ManufOrderStockMoveLineResponse(StockMoveLine stockMoveLine) {
    super(stockMoveLine.getVersion());
    this.stockMoveLineId = stockMoveLine.getId();
  }

  public Long getStockMoveLineId() {
    return stockMoveLineId;
  }
}
