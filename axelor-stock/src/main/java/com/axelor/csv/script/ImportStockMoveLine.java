package com.axelor.csv.script;

import com.axelor.apps.stock.db.StockMoveLine;
import java.util.Map;

public class ImportStockMoveLine {
  public Object importStockMoveLine(Object bean, Map<String, Object> values) {
    assert bean instanceof StockMoveLine;
    StockMoveLine stockMoveLine = (StockMoveLine) bean;
    stockMoveLine.setFromStockLocation(stockMoveLine.getStockMove().getFromStockLocation());
    stockMoveLine.setToStockLocation(stockMoveLine.getStockMove().getToStockLocation());
    return stockMoveLine;
  }
}
