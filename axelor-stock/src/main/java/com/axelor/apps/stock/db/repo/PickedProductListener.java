package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import javax.persistence.PersistenceException;
import javax.persistence.PreRemove;

public class PickedProductListener {

  @PreRemove
  private void onPreRemove(PickedProduct pickedProduct) {
    if (pickedProduct.getStockMoveLine() != null
        && pickedProduct.getStockMoveLine().getStockMove() != null
        && pickedProduct.getStockMoveLine().getStockMove().getStatusSelect()
            == StockMoveRepository.STATUS_REALIZED) {
      Exception e =
          new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_LINE_CANT_DELETE));
      throw new PersistenceException(e);
    }
  }
}
