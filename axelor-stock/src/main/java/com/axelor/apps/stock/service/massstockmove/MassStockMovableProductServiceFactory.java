package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;

public interface MassStockMovableProductServiceFactory {

  MassStockMovableProductProcessingRealizeService<? extends MassStockMovableProduct>
      getMassStockMovableProductProcessingRealizeService(MassStockMovableProduct movableProduct)
          throws AxelorException;

  MassStockMovableProductProcessingCancelService<? extends MassStockMovableProduct>
      getMassStockMovableProductProcessingCancelService(MassStockMovableProduct movableProduct)
          throws AxelorException;

  MassStockMovableProductProcessingSaveService<? extends MassStockMovableProduct>
      getMassStockMovableProductProcessingSaveService(MassStockMovableProduct movableProduct)
          throws AxelorException;

  MassStockMovableProductLocationService<? extends MassStockMovableProduct>
      getMassStockMovableProductLocationService(MassStockMovableProduct movableProduct)
          throws AxelorException;
}
