/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

/**
 * A service which contains all methods managing the reservation feature. The purpose of this
 * service is to update accordingly all reservedQty and requestedReservedQty fields in
 * SaleOrderLine, StockMoveLine and StockLocationLine. The reservation is computed from stock move
 * lines then fields in sale order lines and stock location lines are updated.
 */
public interface ReservedQtyService {

  /**
   * Called on stock move cancel, plan and realization to update requested reserved qty and reserved
   * qty.
   *
   * @param stockMove
   */
  void updateReservedQuantity(StockMove stockMove, int status) throws AxelorException;

  /**
   * For lines with duplicate product, fill all the reserved qty in one line and empty the others.
   *
   * @param stockMove
   */
  void consolidateReservedQtyInStockMoveLineByProduct(StockMove stockMove);

  /**
   * Update requested quantity for internal or external location.
   *
   * @param stockMoveLine
   * @param fromStockLocation
   * @param toStockLocation
   * @param product
   * @param qty the quantity in stock move unit.
   * @param requestedReservedQty the requested reserved quantity in stock move unit.
   * @param toStatus
   * @throws AxelorException
   */
  void updateRequestedQuantityInLocations(
      StockMoveLine stockMoveLine,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      Product product,
      BigDecimal qty,
      BigDecimal requestedReservedQty,
      int toStatus)
      throws AxelorException;

  /**
   * Update location line and stock move line with computed allocated quantity, where the location
   * is {@link com.axelor.apps.stock.db.StockMove#fromStockLocation}
   *
   * @param stockMoveLine a stock move line
   * @param stockLocation a stock location
   * @param product the product of the line. If the product is not managed in stock, this method
   *     does nothing.
   * @param toStatus target status for the stock move
   * @param requestedReservedQty the requested reserved quantity in stock move unit
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
   * @param stockMoveLine a stock move line.
   * @param stockLocation a stock location.
   * @param product the product of the line. If the product is not managed in stock, this method
   *     does nothing.
   * @param toStatus target status for the stock move.
   * @param qty the quantity in stock move unit.
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
   * @param qtyToAllocate the quantity available to be allocated.
   * @param stockLocation a stock location.
   * @param product a product.
   * @param stockLocationLineUnit Unit of the stock location line.
   * @return The quantity that was allocated (in stock location line unit).
   */
  BigDecimal allocateReservedQuantityInSaleOrderLines(
      BigDecimal qtyToAllocate,
      StockLocation stockLocation,
      Product product,
      Unit stockLocationLineUnit)
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
   * Update allocated quantity in sale order line with a new quantity, updating location and moves.
   * If the allocated quantity become bigger than the requested quantity, we also change the
   * requested quantity to match the allocated quantity.
   *
   * @param saleOrderLine
   * @param newReservedQty
   * @throws AxelorException if there is no stock move generated or if we cannot allocate more
   *     quantity.
   */
  void updateReservedQty(SaleOrderLine saleOrderLine, BigDecimal newReservedQty)
      throws AxelorException;

  /**
   * Update requested quantity in sale order line. If the requested quantity become lower than the
   * allocated quantity, we also change the allocated quantity to match the requested quantity.
   *
   * @param saleOrderLine
   * @param newReservedQty
   */
  void updateRequestedReservedQty(SaleOrderLine saleOrderLine, BigDecimal newReservedQty)
      throws AxelorException;

  /**
   * Update allocated quantity in stock move line with a new quantity, updating location. If the
   * allocated quantity become bigger than the requested quantity, we also change the requested
   * quantity to match the allocated quantity.
   *
   * @param stockMoveLine
   * @param newReservedQty
   * @throws AxelorException
   */
  void updateReservedQty(StockMoveLine stockMoveLine, BigDecimal newReservedQty)
      throws AxelorException;

  /**
   * Update requested quantity in stock move line. If the requested quantity become lower than the
   * allocated quantity, we also change the allocated quantity to match the requested quantity.
   *
   * @param stockMoveLine
   * @param newReservedQty
   */
  void updateRequestedReservedQty(StockMoveLine stockMoveLine, BigDecimal newReservedQty)
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
   * @return the new allocated quantity
   * @throws AxelorException
   */
  BigDecimal updateRequestedReservedQuantityInStockMoveLines(
      SaleOrderLine saleOrderLine, Product product, BigDecimal newReservedQty)
      throws AxelorException;

  /**
   * In a partially realized stock move line, call this method to deallocate the quantity that will
   * be allocated to the newly generated stock move line.
   *
   * @param stockMoveLine
   * @param amountToDeallocate
   */
  void deallocateStockMoveLineAfterSplit(StockMoveLine stockMoveLine, BigDecimal amountToDeallocate)
      throws AxelorException;

  /**
   * Update requested reserved qty for stock location line from already updated stock move.
   *
   * @param stockLocationLine
   * @throws AxelorException
   */
  void updateRequestedReservedQty(StockLocationLine stockLocationLine) throws AxelorException;

  /**
   * Request quantity for a sale order line.
   *
   * @param saleOrderLine
   * @throws AxelorException
   */
  void requestQty(SaleOrderLine saleOrderLine) throws AxelorException;

  /**
   * Request quantity for a stock move line.
   *
   * @param stockMoveLine
   * @throws AxelorException
   */
  void requestQty(StockMoveLine stockMoveLine) throws AxelorException;

  /**
   * Cancel the reservation for a sale order line.
   *
   * @param saleOrderLine
   * @throws AxelorException
   */
  void cancelReservation(SaleOrderLine saleOrderLine) throws AxelorException;

  /**
   * Cancel the reservation for a stock move line.
   *
   * @param stockMoveLine
   * @throws AxelorException
   */
  void cancelReservation(StockMoveLine stockMoveLine) throws AxelorException;

  /**
   * Update reserved qty for sale order line from already updated stock move.
   *
   * @param saleOrderLine
   * @throws AxelorException
   */
  void updateReservedQty(SaleOrderLine saleOrderLine) throws AxelorException;

  /**
   * Update reserved qty for stock location line from already updated stock move.
   *
   * @param stockLocationLine
   * @throws AxelorException
   */
  void updateReservedQty(StockLocationLine stockLocationLine) throws AxelorException;

  /**
   * Create a reservation and allocate as much quantity as we can.
   *
   * @param saleOrderLine
   */
  void allocateAll(SaleOrderLine saleOrderLine) throws AxelorException;

  /**
   * Create a reservation and allocate as much quantity as we can.
   *
   * @param stockMoveLine
   */
  void allocateAll(StockMoveLine stockMoveLine) throws AxelorException;
}
