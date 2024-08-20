package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;

public interface MassStockMovableProductProcessingSaveService<T extends MassStockMovableProduct> {
  void save(T movableProduct);
}
