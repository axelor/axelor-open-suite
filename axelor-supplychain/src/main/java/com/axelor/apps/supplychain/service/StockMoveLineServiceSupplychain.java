/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface StockMoveLineServiceSupplychain {

  /**
   * Compared to the method in module stock, it adds the requested reserved qty. Allows to create
   * stock move from supplychain module with requested reserved qty. We also add sale order line and
   * purchase order line to create the link.
   *
   * @param product
   * @param productName
   * @param description
   * @param quantity
   * @param requestedReservedQty
   * @param unitPrice
   * @param unit
   * @param stockMove
   * @param type
   * @param taxed
   * @param taxRate
   * @param saleOrderLine
   * @param purchaseOrderLine
   * @return the created stock move line.
   * @throws AxelorException
   */
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal requestedReservedQty,
      BigDecimal valuatedUnitPrice,
      Unit unit,
      StockMove stockMove,
      int type,
      boolean taxed,
      BigDecimal taxRate,
      SaleOrderLine saleOrderLine,
      PurchaseOrderLine purchaseOrderLine)
      throws AxelorException;

  /**
   * Get a merged stock move line.
   *
   * @param stockMoveLineList
   * @return
   * @throws AxelorException
   */
  StockMoveLine getMergedStockMoveLine(List<StockMoveLine> stockMoveLineList)
      throws AxelorException;

  boolean isAvailableProduct(StockMove stockMove, StockMoveLine stockMoveLine);

  void setInvoiceStatus(StockMoveLine stockMoveLine);
}
