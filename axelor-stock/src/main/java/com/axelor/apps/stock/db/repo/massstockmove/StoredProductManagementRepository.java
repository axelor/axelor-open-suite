package com.axelor.apps.stock.db.repo.massstockmove;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class StoredProductManagementRepository extends StoredProductRepository {

  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;

  @Inject
  public StoredProductManagementRepository(
      MassStockMovableProductQuantityService massStockMovableProductQuantityService) {
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
  }

  @Override
  public StoredProduct save(StoredProduct entity) {

    try {

      entity.setCurrentQty(
          massStockMovableProductQuantityService.getCurrentAvailableQty(
              entity, entity.getMassStockMove().getCartStockLocation()));
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return super.save(entity);
  }
}
