/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SaleOrderStockService {

  /**
   * Create a delivery stock move from a sale order.
   *
   * @param saleOrder
   * @return
   * @throws AxelorException
   */
  public List<Long> createStocksMovesFromSaleOrder(SaleOrder saleOrder) throws AxelorException;

  public StockMove createStockMove(
      SaleOrder saleOrder,
      Company company,
      List<SaleOrderLine> saleOrderLineList,
      String deliveryAddressStr,
      LocalDate estimatedDeliveryDate)
      throws AxelorException;

  public StockMoveLine createStockMoveLine(
      StockMove stockMove,
      SaleOrderLine saleOrderLine,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  public StockMoveLine createStockMoveLine(
      StockMove stockMove,
      SaleOrderLine saleOrderLine,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  public boolean isStockMoveProduct(SaleOrderLine saleOrderLine) throws AxelorException;

  boolean isStockMoveProduct(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  /**
   * Update delivery state by checking delivery states on the sale order lines.
   *
   * @param saleOrder
   */
  void updateDeliveryState(SaleOrder saleOrder) throws AxelorException;

  /**
   * Update delivery states in sale order and sale order lines.
   *
   * @param saleOrder
   * @throws AxelorException
   */
  void fullyUpdateDeliveryState(SaleOrder saleOrder) throws AxelorException;
}
