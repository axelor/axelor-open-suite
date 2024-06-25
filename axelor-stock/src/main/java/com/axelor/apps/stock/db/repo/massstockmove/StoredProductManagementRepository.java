package com.axelor.apps.stock.db.repo.massstockmove;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class StoredProductManagementRepository extends StoredProductRepository {

  protected final MassStockMovableProductService massStockMovableProductService;

  @Inject
  public StoredProductManagementRepository(
      MassStockMovableProductService massStockMovableProductService) {
    this.massStockMovableProductService = massStockMovableProductService;
  }

  @Override
  public StoredProduct save(StoredProduct entity) {

    try {
      entity.setCurrentQty(massStockMovableProductService.getCurrentAvailableQty(entity));
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return super.save(entity);
  }
}
