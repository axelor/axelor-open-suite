package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.service.MassStockMoveSequenceService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class StockMassStockMoveRepository extends MassStockMoveRepository {

  protected MassStockMoveSequenceService massStockMoveSequenceService;

  @Inject
  public StockMassStockMoveRepository(MassStockMoveSequenceService massStockMoveSequenceService) {
    this.massStockMoveSequenceService = massStockMoveSequenceService;
  }

  @Override
  public MassStockMove save(MassStockMove entity) {

    try {

      if (entity.getSequence() == null || entity.getSequence().isBlank()) {
        entity.setSequence(massStockMoveSequenceService.getSequence(entity));
      }
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
    return super.save(entity);
  }
}
