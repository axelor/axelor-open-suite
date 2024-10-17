package com.axelor.apps.stock.db.repo.listener;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import com.axelor.i18n.I18n;
import javax.persistence.PreRemove;

public class MassStockMovableProductListener {

  @PreRemove
  protected void onPreRemove(MassStockMovableProduct movedProduct) throws AxelorException {
    if (movedProduct.getStockMoveLine() != null
        && movedProduct.getStockMoveLine().getStockMove() != null
        && movedProduct.getStockMoveLine().getStockMove().getStatusSelect()
            == StockMoveRepository.STATUS_REALIZED) {

      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_STOCK_MOVE_LINE_CANT_DELETE));
    }
  }
}
