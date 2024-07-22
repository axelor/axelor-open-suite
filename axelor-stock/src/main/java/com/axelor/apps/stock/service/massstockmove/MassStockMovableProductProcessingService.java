package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;

public interface MassStockMovableProductProcessingService<T extends MassStockMovableProduct> {

  void save(T movableProduct);

  void preRealize(T movableProduct) throws AxelorException;

  void postRealize(T movableProduct) throws AxelorException;

  void preCancel(T movableProduct) throws AxelorException;

  void postCancel(T movableProduct) throws AxelorException;
}
