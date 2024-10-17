package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class StoredProductProcessingCancelServiceImpl
    implements MassStockMovableProductProcessingCancelService<StoredProduct> {

  @Override
  public void preCancel(StoredProduct movableProduct) throws AxelorException {
    // Nothing
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void postCancel(StoredProduct movableProduct) throws AxelorException {
    movableProduct.setStoredQty(BigDecimal.ZERO);
    var massStockMove = movableProduct.getMassStockMove();

    massStockMove.setStatusSelect(MassStockMoveRepository.STATUS_IN_PROGRESS);
  }
}
