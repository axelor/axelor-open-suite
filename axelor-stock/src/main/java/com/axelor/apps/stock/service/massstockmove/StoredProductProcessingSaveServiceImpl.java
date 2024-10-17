package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class StoredProductProcessingSaveServiceImpl
    implements MassStockMovableProductProcessingSaveService<StoredProduct> {

  protected final StoredProductRepository storedProductRepository;

  @Inject
  public StoredProductProcessingSaveServiceImpl(StoredProductRepository storedProductRepository) {
    this.storedProductRepository = storedProductRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void save(StoredProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    storedProductRepository.save(movableProduct);
  }
}
