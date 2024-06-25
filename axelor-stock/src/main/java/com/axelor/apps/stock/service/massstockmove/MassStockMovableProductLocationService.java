package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;

public interface MassStockMovableProductLocationService<T extends MassStockMovableProduct> {

  StockLocation getFromStockLocation(T movableProduct);

  StockLocation getToStockLocation(T movableProduct);
}
