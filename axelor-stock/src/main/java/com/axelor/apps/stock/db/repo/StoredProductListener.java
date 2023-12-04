package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import javax.persistence.PersistenceException;
import javax.persistence.PreRemove;

public class StoredProductListener {
  @PreRemove
  private void onPreRemove(StoredProduct storedProduct) {
    if (storedProduct.getStockMoveLine() != null
        && storedProduct.getStockMoveLine().getStockMove() != null
        && storedProduct.getStockMoveLine().getStockMove().getStatusSelect()
            == StockMoveRepository.STATUS_REALIZED) {
      Exception e =
          new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_LINE_CANT_DELETE));
      throw new PersistenceException(e);
    }
  }
}
