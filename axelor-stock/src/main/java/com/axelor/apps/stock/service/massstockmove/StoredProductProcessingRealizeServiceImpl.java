package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class StoredProductProcessingRealizeServiceImpl
    implements MassStockMovableProductProcessingRealizeService<StoredProduct> {

  protected final StoredProductRepository storedProductRepository;
  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;

  @Inject
  public StoredProductProcessingRealizeServiceImpl(
      StoredProductRepository storedProductRepository,
      MassStockMovableProductQuantityService massStockMovableProductQuantityService) {
    this.storedProductRepository = storedProductRepository;
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
  }

  @Override
  public void preRealize(StoredProduct movableProduct) throws AxelorException {
    // Creating a new storedProduct line if not storing everything
    var currentQty =
        massStockMovableProductQuantityService.getCurrentAvailableQty(
            movableProduct, movableProduct.getMassStockMove().getCartStockLocation());
    var commonToStockLocation = movableProduct.getMassStockMove().getCommonToStockLocation();
    if (movableProduct.getStoredQty().compareTo(currentQty) < 0) {
      var newStoredProduct = storedProductRepository.copy(movableProduct, false);
      newStoredProduct.setStoredQty(BigDecimal.ZERO);
      newStoredProduct.setMassStockMove(movableProduct.getMassStockMove());
      newStoredProduct.setStockMoveLine(null);
      if (commonToStockLocation != null) {
        newStoredProduct.setToStockLocation(commonToStockLocation);
      }
      storedProductRepository.save(newStoredProduct);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void postRealize(StoredProduct movableProduct) throws AxelorException {
    var massStockMove = movableProduct.getMassStockMove();

    if (massStockMove.getStoredProductList().stream()
        .allMatch(storedProduct -> storedProduct.getStockMoveLine() != null)) {
      massStockMove.setStatusSelect(MassStockMoveRepository.STATUS_REALIZED);
    }
  }
}
