package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import java.util.List;

public interface ManufOrderUpdateStockMoveService {

  void updateConsumedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  void updateProducedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  void updateResidualStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  void updateStockMoveFromManufOrder(List<StockMoveLine> stockMoveLineList, StockMove stockMove)
      throws AxelorException;
}
