package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import java.util.List;
import java.util.Optional;

public interface ManufOrderGetStockMoveService {
  StockMove getProducedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  StockMove getResidualStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  StockMove getConsumedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  List<StockMove> getResidualOutStockMoveLineList(ManufOrder manufOrder) throws AxelorException;

  List<StockMove> getNonResidualOutStockMoveLineList(ManufOrder manufOrder) throws AxelorException;

  Optional<StockMove> getPlannedStockMove(List<StockMove> stockMoveList);
}
