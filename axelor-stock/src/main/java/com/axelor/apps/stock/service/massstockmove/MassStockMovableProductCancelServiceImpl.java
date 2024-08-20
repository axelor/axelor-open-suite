package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import com.axelor.apps.stock.service.StockMoveService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MassStockMovableProductCancelServiceImpl
    implements MassStockMovableProductCancelService {

  protected final MassStockMovableProductServiceFactory massStockMovableProductServiceFactory;
  protected final StockMoveService stockMoveService;

  @Inject
  public MassStockMovableProductCancelServiceImpl(
      MassStockMovableProductServiceFactory massStockMovableProductServiceFactory,
      StockMoveService stockMoveService) {
    this.massStockMovableProductServiceFactory = massStockMovableProductServiceFactory;
    this.stockMoveService = stockMoveService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancel(List<? extends MassStockMovableProduct> massStockMovableProducts)
      throws AxelorException {
    Objects.requireNonNull(massStockMovableProducts);

    if (!massStockMovableProducts.isEmpty()) {
      MassStockMovableProductProcessingCancelService processingService =
          massStockMovableProductServiceFactory.getMassStockMovableProductProcessingCancelService(
              massStockMovableProducts.get(0));
      MassStockMovableProductProcessingSaveService saveService =
          massStockMovableProductServiceFactory.getMassStockMovableProductProcessingSaveService(
              massStockMovableProducts.get(0));

      for (MassStockMovableProduct massStockMovableProduct : massStockMovableProducts) {
        cancel(massStockMovableProduct, processingService, saveService);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancel(MassStockMovableProduct movableProduct) throws AxelorException {

    MassStockMovableProductProcessingCancelService processingService =
        massStockMovableProductServiceFactory.getMassStockMovableProductProcessingCancelService(
            movableProduct);
    MassStockMovableProductProcessingSaveService saveService =
        massStockMovableProductServiceFactory.getMassStockMovableProductProcessingSaveService(
            movableProduct);

    cancel(movableProduct, processingService, saveService);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void cancel(
      MassStockMovableProduct movableProduct,
      MassStockMovableProductProcessingCancelService processingService,
      MassStockMovableProductProcessingSaveService saveService)
      throws AxelorException {
    Objects.requireNonNull(movableProduct);

    if (movableProduct.getStockMoveLine() != null) {
      processingService.preCancel(movableProduct);
      stockMoveService.cancel(movableProduct.getStockMoveLine().getStockMove());
      movableProduct.setStockMoveLine(null);
      movableProduct.setMovedQty(BigDecimal.ZERO);
      processingService.postCancel(movableProduct);
      saveService.save(movableProduct);
    }
  }
}
