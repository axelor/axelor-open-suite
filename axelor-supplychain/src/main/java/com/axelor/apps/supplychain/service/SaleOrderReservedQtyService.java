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

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;

public interface SaleOrderReservedQtyService {

  /**
   * Try to allocate every line, meaning the allocated quantity of the line will be changed to match
   * the requested quantity.
   *
   * @param saleOrder a confirmed sale order.
   * @throws AxelorException if the sale order does not have a stock move.
   */
  void allocateAll(SaleOrder saleOrder) throws AxelorException;

  /**
   * Deallocate every line, meaning the allocated quantity of the line will be changed to 0.
   *
   * @param saleOrder a confirmed sale order.
   * @throws AxelorException if the sale order does not have a stock move.
   */
  void deallocateAll(SaleOrder saleOrder) throws AxelorException;

  /**
   * Reserve the quantity for every line, meaning we change both requested and allocated quantity to
   * the quantity of the line.
   *
   * @param saleOrder
   * @throws AxelorException if the sale order does not have a stock move.
   */
  void reserveAll(SaleOrder saleOrder) throws AxelorException;

  /**
   * Cancel the reservation for every line, meaning we change both requested and allocated quantity
   * to 0.
   *
   * @param saleOrder
   * @throws AxelorException if the sale order does not have a stock move.
   */
  void cancelReservation(SaleOrder saleOrder) throws AxelorException;
}
