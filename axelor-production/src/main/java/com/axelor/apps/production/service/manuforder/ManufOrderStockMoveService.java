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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import java.math.BigDecimal;
import java.util.List;

public interface ManufOrderStockMoveService {

  /**
   * Given a manuf order, its company determine the in default stock location and return it. First
   * search in prodprocess, then in company stock configuration.
   *
   * @param manufOrder a manufacturing order.
   * @param company a company with stock config.
   * @return the found stock location, which can be null.
   * @throws AxelorException if the stock config is missing for the company.
   */
  StockLocation getDefaultInStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  /**
   * Given a manuf order, its company determine the out default stock location and return it. First
   * search in prodprocess, then in company stock configuration.
   *
   * @param manufOrder a manufacturing order.
   * @param company a company with stock config.
   * @return the found stock location, which can be null.
   * @throws AxelorException if the stock config is missing for the company.
   */
  StockLocation getDefaultOutStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  /**
   * Consume in stock moves in manuf order.
   *
   * @param manufOrder
   * @throws AxelorException
   */
  void consumeInStockMoves(ManufOrder manufOrder) throws AxelorException;

  void finish(ManufOrder manufOrder) throws AxelorException;

  void finishStockMove(StockMove stockMove) throws AxelorException;

  /**
   * Call the method to realize in stock move, then the method to realize out stock move for the
   * given manufacturing order.
   *
   * @param manufOrder
   */
  void partialFinish(ManufOrder manufOrder) throws AxelorException;

  void cancel(ManufOrder manufOrder) throws AxelorException;

  void cancel(StockMove stockMove) throws AxelorException;

  /**
   * Compute the right qty when modifying real quantity in a manuf order
   *
   * @param manufOrder
   * @param prodProduct
   * @param qtyToUpdate
   * @return
   */
  BigDecimal getFractionQty(ManufOrder manufOrder, ProdProduct prodProduct, BigDecimal qtyToUpdate);

  StockLocation getFromStockLocationForConsumedStockMove(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation getVirtualStockLocationForConsumedStockMove(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation _getVirtualProductionStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation _getVirtualOutsourcingStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation getProducedProductStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation getResidualProductStockLocation(ManufOrder manufOrder) throws AxelorException;

  StockLocation getVirtualStockLocationForProducedStockMove(ManufOrder manufOrder, Company company)
      throws AxelorException;

  public List<Long> getOutgoingStockMoves(ManufOrder manufOrder);
}
