package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import java.util.Optional;

public interface ManufOrderPlanStockMoveService {
  Optional<StockMove> createAndPlanToProduceStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException;

  Optional<StockMove> createAndPlanResidualStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException;

  Optional<StockMove> createAndPlanToConsumeStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException;

  Optional<StockMove> createAndPlanToConsumeStockMove(ManufOrder manufOrder) throws AxelorException;
}
