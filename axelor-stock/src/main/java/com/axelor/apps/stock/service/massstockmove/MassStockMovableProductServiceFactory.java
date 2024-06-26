package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;

public interface MassStockMovableProductServiceFactory {

  MassStockMovableProductProcessingService<? extends MassStockMovableProduct>
      getMassStockMovableProductProcessingService(MassStockMovableProduct movableProduct)
          throws AxelorException;
}
