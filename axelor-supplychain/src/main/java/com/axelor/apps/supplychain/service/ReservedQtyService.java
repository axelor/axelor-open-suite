/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface ReservedQtyService {

  /**
   * Called on stock move cancel, plan and realization to update requested reserved qty and reserved
   * qty.
   *
   * @param stockMove
   */
  void updateReservedQuantity(StockMove stockMove, int status) throws AxelorException;

  /**
   * Update requested quantity for internal or external location.
   *
   * @param stockMoveLine
   * @param fromStockLocation
   * @param toStockLocation
   * @param product
   * @param qty
   * @param toStatus
   * @throws AxelorException
   */
  void updateRequestedQuantityInLocations(
      StockMoveLine stockMoveLine,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      Product product,
      BigDecimal qty,
      BigDecimal convertedRequestedReservedQty,
      int toStatus)
      throws AxelorException;

  /**
   * Update location line and stock move line with computed allocated quantity, where the location
   * is {@link com.axelor.apps.stock.db.StockMove#fromStockLocation}
   *
   * @param stockMoveLine a stock move line
   * @param stockLocation a stock location
   * @param product the product of the line
   * @param toStatus target status for the stock move
   * @param requestedReservedQty the requested reserved quantity, converted in product unit.
   * @throws AxelorException
   */
  void updateRequestedQuantityInFromStockLocation(
      StockMoveLine stockMoveLine,
      StockLocation stockLocation,
      Product product,
      int toStatus,
      BigDecimal requestedReservedQty)
      throws AxelorException;
  /**
   * Update location line, stock move line and sale order line with computed allocated quantity,
   * where the location is {@link com.axelor.apps.stock.db.StockMove#toStockLocation}.
   *
   * @param stockMoveLine a stock move line
   * @param stockLocation a stock location
   * @param product the product of the line
   * @param toStatus target status for the stock move
   * @param qty
   * @throws AxelorException
   */
  void updateRequestedQuantityInToStockLocation(
      StockMoveLine stockMoveLine,
      StockLocation stockLocation,
      Product product,
      int toStatus,
      BigDecimal qty)
      throws AxelorException;

  /**
   * Allocate a given quantity in stock move lines and sale order lines corresponding to the given
   * product and stock location. The first stock move to have the reservation will be the first to
   * have the quantity allocated.
   *
   * @param qtyToAllocate
   * @param stockLocation
   * @param product
   */
  void allocateReservedQuantityInSaleOrderLines(
      BigDecimal qtyToAllocate, StockLocation stockLocation, Product product)
      throws AxelorException;

  /**
   * From the requested reserved quantity, return the quantity that can in fact be reserved.
   *
   * @param stockLocationLine the location line.
   * @param requestedReservedQty the quantity that can be added to the real quantity
   * @return the quantity really added.
   */
  BigDecimal computeRealReservedQty(
      StockLocationLine stockLocationLine, BigDecimal requestedReservedQty);

  /**
   * Update allocated quantity in sale order line.
   *
   * @param saleOrderLine
   * @param newReservedQty
   * @throws AxelorException if there is no stock move generated or if we cannot allocate more
   *     quantity.
   */
  void updateReservedQty(SaleOrderLine saleOrderLine, BigDecimal newReservedQty)
      throws AxelorException;

  /**
   * Update requested quantity in sale order line.
   *
   * @param saleOrderLine
   * @param newReservedQty
   */
  void updateRequestedReservedQty(SaleOrderLine saleOrderLine, BigDecimal newReservedQty)
      throws AxelorException;

  /**
   * Update reserved quantity in stock move lines and sale order lines from stock move lines.
   *
   * @param stockMoveLine
   * @param product
   * @param reservedQtyToAdd
   */
  void updateReservedQuantityFromStockMoveLine(
      StockMoveLine stockMoveLine, Product product, BigDecimal reservedQtyToAdd)
      throws AxelorException;

  /**
   * Update reserved quantity in stock move lines from sale order line. Manage the case of split
   * stock move lines.
   *
   * @param saleOrderLine
   * @param product
   * @param newReservedQty
   * @throws AxelorException
   */
  void updateReservedQuantityInStockMoveLineFromSaleOrderLine(
      SaleOrderLine saleOrderLine, Product product, BigDecimal newReservedQty)
      throws AxelorException;

  /**
   * Update requested reserved quantity in stock move lines from sale order line. Manage the case of
   * split stock move lines.
   *
   * @param saleOrderLine
   * @param product
   * @param newReservedQty
   * @throws AxelorException
   */
  void updateRequestedReservedQuantityInStockMoveLines(
      SaleOrderLine saleOrderLine, Product product, BigDecimal newReservedQty)
      throws AxelorException;
}
