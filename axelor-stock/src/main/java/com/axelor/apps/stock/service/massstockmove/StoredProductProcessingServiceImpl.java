package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Objects;

public class StoredProductProcessingServiceImpl
    implements MassStockMovableProductProcessingService<StoredProduct> {

  protected final StoredProductRepository storedProductRepository;

  @Inject
  public StoredProductProcessingServiceImpl(StoredProductRepository storedProductRepository) {
    this.storedProductRepository = storedProductRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void save(StoredProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    storedProductRepository.save(movableProduct);
  }

  @Override
  public StockLocation getFromStockLocation(StoredProduct movableProduct) {
    Objects.requireNonNull(movableProduct);

    return movableProduct.getMassStockMove().getCartStockLocation();
  }

  @Override
  public StockLocation getToStockLocation(StoredProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    return movableProduct.getStockLocation();
  }

  @Override
  public void preRealize(StoredProduct movableProduct) {
    Objects.requireNonNull(movableProduct);

    // Creating a new storedProduct line if not storing everything
    if (movableProduct.getStoredQty().compareTo(movableProduct.getCurrentQty()) < 0) {
      var newStoredProduct = storedProductRepository.copy(movableProduct, false);
      newStoredProduct.setStoredQty(BigDecimal.ZERO);
      newStoredProduct.setCurrentQty(
          movableProduct.getCurrentQty().subtract(movableProduct.getStoredQty()));
      newStoredProduct.setMassStockMove(movableProduct.getMassStockMove());
      storedProductRepository.save(newStoredProduct);
    }
  }

  @Override
  public void postRealize(StoredProduct movableProduct) {
    // NOTHING
  }
}
