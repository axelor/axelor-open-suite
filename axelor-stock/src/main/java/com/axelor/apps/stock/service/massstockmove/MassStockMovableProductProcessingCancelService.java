package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;

public interface MassStockMovableProductProcessingCancelService<T extends MassStockMovableProduct> {

  void preCancel(T movableProduct) throws AxelorException;

  void postCancel(T movableProduct) throws AxelorException;
}
