package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Objects;

public class StoredProductProcessingServiceImpl
    implements MassStockMovableProductProcessingService<StoredProduct> {

  protected final StoredProductRepository storedProductRepository;
  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;

  @Inject
  public StoredProductProcessingServiceImpl(
      StoredProductRepository storedProductRepository,
      MassStockMovableProductQuantityService massStockMovableProductQuantityService) {
    this.storedProductRepository = storedProductRepository;
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void save(StoredProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    storedProductRepository.save(movableProduct);
  }

  @Override
  public void preRealize(StoredProduct movableProduct) {
    // NOTHING

  }

  @Override
  public void postRealize(StoredProduct movableProduct) throws AxelorException {
    // Creating a new storedProduct line if not storing everything
    var currentQty =
        massStockMovableProductQuantityService.getCurrentAvailableQty(
            movableProduct, movableProduct.getMassStockMove().getCartStockLocation());
    if (movableProduct.getStoredQty().compareTo(currentQty) < 0) {
      var newStoredProduct = storedProductRepository.copy(movableProduct, false);
      newStoredProduct.setStoredQty(BigDecimal.ZERO);
      newStoredProduct.setMassStockMove(movableProduct.getMassStockMove());
      newStoredProduct.setStockMoveLine(null);
      storedProductRepository.save(newStoredProduct);
    }
  }
}
