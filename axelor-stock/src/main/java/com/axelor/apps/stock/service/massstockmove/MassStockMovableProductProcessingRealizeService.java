package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;

public interface MassStockMovableProductProcessingRealizeService<
    T extends MassStockMovableProduct> {

  void preRealize(T movableProduct) throws AxelorException;

  void postRealize(T movableProduct) throws AxelorException;
}
