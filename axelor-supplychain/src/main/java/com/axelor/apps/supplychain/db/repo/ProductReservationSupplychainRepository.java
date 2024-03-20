package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class ProductReservationSupplychainRepository extends ProductReservationRepository {

  @Override
  public ProductReservation save(ProductReservation entity) {
    try {
      if (entity.getStatus() == null || entity.getStatus() == 0) {
        entity = Beans.get(ProductReservationService.class).updateStatus(entity);
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
    return super.save(entity);
  }
}
