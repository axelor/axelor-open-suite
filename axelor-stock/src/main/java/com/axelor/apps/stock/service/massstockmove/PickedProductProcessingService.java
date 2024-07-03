package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class PickedProductProcessingService
    implements MassStockMovableProductProcessingService<PickedProduct> {

  protected final PickedProductRepository pickedProductRepository;

  @Inject
  public PickedProductProcessingService(PickedProductRepository pickedProductRepository) {
    this.pickedProductRepository = pickedProductRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void save(PickedProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    pickedProductRepository.save(movableProduct);
  }
}
