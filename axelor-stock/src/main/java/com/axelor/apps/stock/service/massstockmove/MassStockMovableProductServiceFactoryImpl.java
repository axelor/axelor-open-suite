package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import com.google.inject.Inject;
import java.util.Objects;

public class MassStockMovableProductServiceFactoryImpl
    implements MassStockMovableProductServiceFactory {

  protected final PickedProductProcessingService pickedProductProcessingService;

  @Inject
  public MassStockMovableProductServiceFactoryImpl(
      PickedProductProcessingService pickedProductProcessingService) {
    this.pickedProductProcessingService = pickedProductProcessingService;
  }

  @Override
  public MassStockMovableProductProcessingService<? extends MassStockMovableProduct>
      getMassStockMovableProductProcessingService(MassStockMovableProduct movableProduct)
          throws AxelorException {

    Objects.requireNonNull(movableProduct);

    if (movableProduct instanceof PickedProduct) {
      return pickedProductProcessingService;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        StockExceptionMessage.STOCK_MOVE_MASS_FACTORY_UNKNOWN_OBJECT);
  }
}
