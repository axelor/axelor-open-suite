package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
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

  @Inject
  public PickedProductProcessingServiceImpl(
      PickedProductRepository pickedProductRepository,
      PickedProductService pickedProductService,
      StoredProductRepository storedProductRepository) {
    this.pickedProductRepository = pickedProductRepository;
    this.pickedProductService = pickedProductService;
    this.storedProductRepository = storedProductRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void save(PickedProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    pickedProductRepository.save(movableProduct);
  }

  @Override
  public StockLocation getFromStockLocation(PickedProduct movableProduct) {
    Objects.requireNonNull(movableProduct);

    return movableProduct.getStockLocation();
  }

  @Override
  public StockLocation getToStockLocation(PickedProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    return movableProduct.getMassStockMove().getCartStockLocation();
  }

  @Override
  public void preRealize(PickedProduct movableProduct) {
    // NOTHING
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void postRealize(PickedProduct movableProduct) {
    Objects.requireNonNull(movableProduct);

    var createdStoredProduct = pickedProductService.createFromPickedProduct(movableProduct);

    storedProductRepository.save(createdStoredProduct);
    pickedProductRepository.save(movableProduct);
  }
}
