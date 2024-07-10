package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import com.axelor.inject.Beans;
import java.util.Objects;

public class MassStockMovableProductServiceFactoryImpl
    implements MassStockMovableProductServiceFactory {

  @Override
  public MassStockMovableProductProcessingService<? extends MassStockMovableProduct>
      getMassStockMovableProductProcessingService(MassStockMovableProduct movableProduct)
          throws AxelorException {

    Objects.requireNonNull(movableProduct);

    if (movableProduct instanceof PickedProduct) {
      return Beans.get(PickedProductProcessingServiceImpl.class);
    } else if (movableProduct instanceof StoredProduct) {
      return Beans.get(StoredProductProcessingServiceImpl.class);
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        StockExceptionMessage.STOCK_MOVE_MASS_FACTORY_UNKNOWN_OBJECT);
  }

  @Override
  public MassStockMovableProductLocationService<? extends MassStockMovableProduct>
      getMassStockMovableProductLocationService(MassStockMovableProduct movableProduct)
          throws AxelorException {
    Objects.requireNonNull(movableProduct);

    if (movableProduct instanceof PickedProduct) {
      return Beans.get(PickedProductLocationServiceImpl.class);
    } else if (movableProduct instanceof StoredProduct) {
      return Beans.get(StoredProductLocationServiceImpl.class);
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        StockExceptionMessage.STOCK_MOVE_MASS_FACTORY_UNKNOWN_OBJECT);
  }
}
