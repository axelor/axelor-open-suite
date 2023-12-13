/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ManufOrderStockMoveService {

  Optional<StockMove> createAndPlanToConsumeStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException;

  Optional<StockMove> createAndPlanToConsumeStockMove(ManufOrder manufOrder) throws AxelorException;

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

  Optional<StockMove> createAndPlanToProduceStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException;

  Optional<StockMove> createAndPlanToProduceStockMove(ManufOrder manufOrder) throws AxelorException;

  /**
   * Consume in stock moves in manuf order.
   *
   * @param manufOrder
   * @throws AxelorException
   */
  void consumeInStockMoves(ManufOrder manufOrder) throws AxelorException;

  StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  void finish(ManufOrder manufOrder) throws AxelorException;

  void finishStockMove(StockMove stockMove) throws AxelorException;

  /**
   * Call the method to realize in stock move, then the method to realize out stock move for the
   * given manufacturing order.
   *
   * @param manufOrder
   */
  void partialFinish(ManufOrder manufOrder) throws AxelorException;

  /**
   * Allows to create and realize in or out stock moves for the given manufacturing order.
   *
   * @param manufOrder
   * @param inOrOut can be {@link ManufOrderStockMoveService#PART_FINISH_IN} or {@link
   *     ManufOrderStockMoveService#PART_FINISH_OUT}
   * @throws AxelorException
   */

  /**
   * Get the planned stock move in a stock move list
   *
   * @param stockMoveList can be {@link ManufOrder#inStockMoveList} or {@link
   *     ManufOrder#outStockMoveList}
   * @return an optional stock move
   */
  Optional<StockMove> getPlannedStockMove(List<StockMove> stockMoveList);

  /**
   * Generate stock move lines after a partial finish
   *
   * @param manufOrder
   * @param stockMove
   * @param inOrOut can be {@link ManufOrderStockMoveService#PART_FINISH_IN} or {@link
   *     ManufOrderStockMoveService#PART_FINISH_OUT}
   */
  void createNewStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      int inOrOut,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;
  /**
   * Generate stock move lines after a partial finish
   *
   * @param diffProdProductList
   * @param stockMove
   * @param stockMoveLineType
   * @throws AxelorException
   */
  void createNewStockMoveLines(
      List<ProdProduct> diffProdProductList,
      StockMove stockMove,
      int stockMoveLineType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  void cancel(ManufOrder manufOrder) throws AxelorException;

  void cancel(StockMove stockMove) throws AxelorException;

  /**
   * Clear the consumed list and create a new one with the right quantity.
   *
   * @param manufOrder
   * @param qtyToUpdate
   */
  void createNewConsumedStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException;

  /**
   * Clear the produced list and create a new one with the right quantity.
   *
   * @param manufOrder
   * @param qtyToUpdate
   */
  void createNewProducedStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException;

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

  StockLocation getProducedProductStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException;

  StockLocation getVirtualStockLocationForProducedStockMove(ManufOrder manufOrder, Company company)
      throws AxelorException;

  public List<Long> getOutgoingStockMoves(ManufOrder manufOrder);
}
