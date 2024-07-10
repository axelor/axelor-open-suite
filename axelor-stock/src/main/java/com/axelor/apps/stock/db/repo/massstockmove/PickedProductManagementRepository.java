package com.axelor.apps.stock.db.repo.massstockmove;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class PickedProductManagementRepository extends PickedProductRepository {

  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;

  @Inject
  public PickedProductManagementRepository(
      MassStockMovableProductQuantityService massStockMovableProductQuantityService) {
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
  }

  @Override
  public PickedProduct save(PickedProduct entity) {

    try {
      entity.setCurrentQty(
          massStockMovableProductQuantityService.getCurrentAvailableQty(
              entity, entity.getFromStockLocation()));
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return super.save(entity);
  }
}
