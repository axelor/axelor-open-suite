package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.tool.api.ResponseStructure;

public class StockMoveLineResponse extends ResponseStructure {
  private final long id;
  private final long stockMoveId;
  private final long productId;
  private final int realQty;
  private final int conformity;

  public StockMoveLineResponse(StockMoveLine stockMoveLine) {
    super(stockMoveLine.getVersion());
    this.id = stockMoveLine.getId();
    this.stockMoveId = stockMoveLine.getStockMove().getId();
    this.productId = stockMoveLine.getProduct().getId();
    this.realQty = stockMoveLine.getRealQty().intValue();
    this.conformity = stockMoveLine.getConformitySelect();
  }

  public long getId() {
    return id;
  }

  public long getStockMoveId() {
    return stockMoveId;
  }

  public long getProductId() {
    return productId;
  }

  public int getRealQty() {
    return realQty;
  }

  public int getConformity() {
    return conformity;
  }
}
