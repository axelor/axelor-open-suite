package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;

public interface MassStockMovableProductProcessingService<T extends MassStockMovableProduct> {

  void save(T movableProduct);

  StockLocation getFromStockLocation(T movableProduct);

  StockLocation getToStockLocation(T movableProduct);

  void preRealize(T movableProduct);

  void postRealize(T movableProduct);
}
