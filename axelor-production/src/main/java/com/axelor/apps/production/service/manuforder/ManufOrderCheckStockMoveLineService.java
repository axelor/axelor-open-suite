package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMoveLine;
import java.util.List;

public interface ManufOrderCheckStockMoveLineService {

  /**
   * Check the realized consumed stock move lines in manuf order has not changed.
   *
   * @param manufOrder a manuf order from context.
   * @param oldManufOrder a manuf order from database.
   * @throws AxelorException if the check fails.
   */
  void checkConsumedStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException;

  /**
   * Check the realized produced stock move lines in manuf order has not changed.
   *
   * @param manufOrder a manuf order from context.
   * @param oldManufOrder a manuf order from database.
   * @throws AxelorException if the check fails.
   */
  void checkProducedStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException;

  void checkResidualStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException;

  /**
   * Check between a new and an old stock move line list whether a realized stock move line has been
   * deleted.
   *
   * @param stockMoveLineList a stock move line list from view context.
   * @param oldStockMoveLineList a stock move line list from database.
   * @throws AxelorException if the check fails.
   */
  void checkRealizedStockMoveLineList(
      List<StockMoveLine> stockMoveLineList, List<StockMoveLine> oldStockMoveLineList)
      throws AxelorException;
}
