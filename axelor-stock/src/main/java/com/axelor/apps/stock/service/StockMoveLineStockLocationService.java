package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;

public interface StockMoveLineStockLocationService {

  void fillStockLocationWithDefaultStockLocation(StockMoveLine stockMoveLine, StockMove stockMove);

  StockLocation getDefaultFromStockLocation(StockMoveLine stockMoveLine, StockMove stockMove);

  StockLocation getDefaultToStockLocation(StockMoveLine stockMoveLine, StockMove stockMove);

  String getStockLocationDomainWithDefaultLocation(
      StockMoveLine stockMoveLine, StockMove stockMove, StockLocation stockLocation);
}
