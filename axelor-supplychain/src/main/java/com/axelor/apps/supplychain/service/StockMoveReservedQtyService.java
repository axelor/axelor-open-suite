package com.axelor.apps.supplychain.service;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;

public interface StockMoveReservedQtyService {

  /**
   * Try to allocate every line, meaning the allocated quantity of the line will be changed to match
   * the requested quantity. Ignore line with real qty at 0.
   *
   * @param stockMove a planned stock move.
   * @throws AxelorException if the sale order does not have a stock move.
   */
  void allocateAll(StockMove stockMove) throws AxelorException;
}
