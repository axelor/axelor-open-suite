package com.axelor.apps.stock.service.batch.model;

import com.axelor.apps.stock.db.StockMoveLine;
import java.util.Objects;

public class StockMoveLineOrigin {

  private final StockMoveLine stockMoveLine;
  private final String origin;

  public StockMoveLineOrigin(StockMoveLine stockMoveLine, String origin) {
    this.stockMoveLine = Objects.requireNonNull(stockMoveLine);
    this.origin = origin;
  }

  public StockMoveLine getStockMoveLine() {
    return stockMoveLine;
  }

  public String getOrigin() {
    return origin;
  }

  public StockMoveLineOrigin merge(StockMoveLineOrigin stockMoveLineOriginToBeMerged) {

    this.stockMoveLine.setQty(
        this.stockMoveLine.getQty().add(stockMoveLineOriginToBeMerged.getStockMoveLine().getQty()));
    this.stockMoveLine.setRealQty(
        this.stockMoveLine
            .getRealQty()
            .add(stockMoveLineOriginToBeMerged.getStockMoveLine().getRealQty()));

    return new StockMoveLineOrigin(
        this.stockMoveLine,
        String.format("%s, %s", this.origin, stockMoveLineOriginToBeMerged.getOrigin()));
  }
}
