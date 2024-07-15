package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class PickedProductProcessingServiceImpl
    implements MassStockMovableProductProcessingService<PickedProduct> {

  protected final PickedProductRepository pickedProductRepository;
  protected final PickedProductService pickedProductService;
  protected final StoredProductRepository storedProductRepository;
  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;

  @Inject
  public PickedProductProcessingServiceImpl(
      PickedProductRepository pickedProductRepository,
      PickedProductService pickedProductService,
      StoredProductRepository storedProductRepository,
      MassStockMovableProductQuantityService massStockMovableProductQuantityService) {
    this.pickedProductRepository = pickedProductRepository;
    this.pickedProductService = pickedProductService;
    this.storedProductRepository = storedProductRepository;
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void save(PickedProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    pickedProductRepository.save(movableProduct);
  }

  @Override
  public void preRealize(PickedProduct movableProduct) {
    // NOTHING
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void postRealize(PickedProduct movableProduct) throws AxelorException {
    Objects.requireNonNull(movableProduct);

    var createdStoredProduct = pickedProductService.createFromPickedProduct(movableProduct);

    storedProductRepository.save(createdStoredProduct);
    pickedProductRepository.save(movableProduct);
  }
}
