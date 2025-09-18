package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;

public interface StockMoveLineStockLocationService {

  void fillStockLocationWithDefaultStockLocation(StockMoveLine stockMoveLine, StockMove stockMove);

  StockLocation getDefaultFromStockLocation(Product product, StockMove stockMove);

  StockLocation getDefaultToStockLocation(Product product, StockMove stockMove);

  String getStockLocationDomainWithDefaultLocation(
      StockMoveLine stockMoveLine, StockMove stockMove, StockLocation stockLocation);
}
