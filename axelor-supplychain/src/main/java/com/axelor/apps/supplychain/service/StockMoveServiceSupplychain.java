/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;

public interface StockMoveServiceSupplychain {

  /**
   * For all lines in this stock move with quantity equal to 0, we empty the link to sale order
   * lines, allowing to delete non delivered sale order lines.
   *
   * @param stockMove
   */
  void detachNonDeliveredStockMoveLines(StockMove stockMove);

  void verifyProductStock(StockMove stockMove) throws AxelorException;

  public boolean isAllocatedStockMoveLineRemoved(StockMove stockMove);

  public void setDefaultInvoicedPartner(StockMove stockMove);

  void checkInvoiceStatus(StockMove stockMove) throws AxelorException;

  public void setInvoicingStatusInvoicedDelayed(StockMove stockMove);

  public void setInvoicingStatusInvoicedValidated(StockMove stockMove);
}
