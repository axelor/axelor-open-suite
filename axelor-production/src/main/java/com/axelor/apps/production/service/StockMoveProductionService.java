package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveService;

public interface StockMoveProductionService extends StockMoveService {

  /**
   * Only call this method when you are currently cancelling a manufacturing order. This is
   * bypassing the check on existing MO to allow the stock move to be cancelled.
   *
   * @param stockMove a stock move linked to a manufacturing order.
   * @throws AxelorException
   */
  void cancelFromManufOrder(StockMove stockMove) throws AxelorException;
}
