package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.service.MassStockMoveService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class StockMassStockMoveRepository extends MassStockMoveRepository {

  @Override
  public MassStockMove save(MassStockMove entity) {

    try {

      if (entity.getSequence() == null || entity.getSequence().isBlank()) {
        entity.setSequence(Beans.get(MassStockMoveService.class).getSequence(entity));
      }
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
    return super.save(entity);
  }
}
