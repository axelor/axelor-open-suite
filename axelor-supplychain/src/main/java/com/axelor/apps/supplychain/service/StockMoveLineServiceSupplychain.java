package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface StockMoveLineServiceSupplychain {

  /**
   * Compared to the method in module stock, it adds the reserved qty. Allows to create stock move
   * from supplychain module with reserved qty.
   *
   * @param product
   * @param productName
   * @param description
   * @param quantity
   * @param reservedQty
   * @param unitPrice
   * @param unit
   * @param stockMove
   * @param type
   * @param taxed
   * @param taxRate
   * @return the created stock move line.
   * @throws AxelorException
   */
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal reservedQty,
      BigDecimal unitPrice,
      Unit unit,
      StockMove stockMove,
      int type,
      boolean taxed,
      BigDecimal taxRate)
      throws AxelorException;
}
