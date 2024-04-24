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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationLineHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StockLocationLineService {

  public void updateLocation(
      StockLocation stockLocation,
      Product product,
      Unit stockMoveLineUnit,
      BigDecimal qty,
      boolean current,
      boolean future,
      boolean isIncrement,
      LocalDate lastFutureStockMoveDate,
      TrackingNumber trackingNumber,
      boolean generateOrder)
      throws AxelorException;

  public void updateLocation(
      StockLocation stockLocation,
      Product product,
      Unit stockMoveLineUnit,
      BigDecimal qty,
      boolean current,
      boolean future,
      boolean isIncrement,
      LocalDate lastFutureStockMoveDate,
      boolean generateOrder)
      throws AxelorException;

  public void minStockRules(
      Product product,
      BigDecimal qty,
      StockLocationLine stockLocationLine,
      boolean current,
      boolean future)
      throws AxelorException;

  public void maxStockRules(
      Product product,
      BigDecimal qty,
      StockLocationLine stockLocationLine,
      boolean current,
      boolean future)
      throws AxelorException;

  public void updateDetailLocation(
      StockLocation stockLocation,
      Product product,
      Unit stockMoveLineUnit,
      BigDecimal qty,
      boolean current,
      boolean future,
      boolean isIncrement,
      LocalDate lastFutureStockMoveDate,
      TrackingNumber trackingNumber)
      throws AxelorException;

  public void checkStockMin(StockLocationLine stockLocationLine, boolean isDetailLocationLine)
      throws AxelorException;

  /**
   * Check if the stock location has enough qty of the product in the given unit.
   *
   * @param stockLocation
   * @param product
   * @param unit
   * @param qty
   * @throws AxelorException if there is not enough qty in stock
   */
  void checkIfEnoughStock(StockLocation stockLocation, Product product, Unit unit, BigDecimal qty)
      throws AxelorException;

  public StockLocationLine updateLocation(
      StockLocationLine stockLocationLine,
      Unit stockMoveLineUnit,
      Product product,
      BigDecimal qty,
      boolean current,
      boolean future,
      boolean isIncrement,
      LocalDate lastFutureStockMoveDate)
      throws AxelorException;

  BigDecimal getTrackingNumberAvailableQty(TrackingNumber trackingNumber);

  public void updateStockLocationFromProduct(StockLocationLine stockLocationLine, Product product)
      throws AxelorException;

  public StockLocationLine updateLocationFromProduct(
      StockLocationLine stockLocationLine, Product product) throws AxelorException;

  /**
   * Getting the stock location line : We check if the location has a detailed line for a given
   * product. If no detailed location line is found, we create it.
   *
   * @param stockLocation A location
   * @param product A product
   * @return The found or created location line
   */
  public StockLocationLine getOrCreateStockLocationLine(
      StockLocation stockLocation, Product product);

  /**
   * Getting the detailed stock location line : We check if the location has a detailed line for a
   * given product, product variant and tracking number. If no detailed location line is found, we
   * create it.
   *
   * @param detailLocation A location
   * @param product A product
   * @param trackingNumber A tracking number
   * @return The found or created detailed location line
   */
  public StockLocationLine getOrCreateDetailLocationLine(
      StockLocation detailLocation, Product product, TrackingNumber trackingNumber);

  /**
   * Allow to get the location line of a given product in a given location.
   *
   * @param stockLocation A location
   * @param product A product
   * @return The stock location line if found, else null
   */
  public StockLocationLine getStockLocationLine(StockLocation stockLocation, Product product);

  /**
   * Allow to get the location lines of a given product.
   *
   * @param product
   * @return
   */
  public List<StockLocationLine> getStockLocationLines(Product product);

  /**
   * Allow to get the detailed location line of a given product, product variant and tracking number
   * in a given location.
   *
   * @param stockLocation A location
   * @param product A product
   * @param trackingNumber A tracking number
   * @return The stock location line if found, else null
   */
  public StockLocationLine getDetailLocationLine(
      StockLocation stockLocation, Product product, TrackingNumber trackingNumber);

  List<StockLocationLine> getDetailLocationLines(Product product, TrackingNumber trackingNumber);

  /**
   * Allow the creation of a location line of a given product in a given location.
   *
   * @param stockLocation A location
   * @param product A product
   * @return The created stock location line
   */
  public StockLocationLine createLocationLine(StockLocation stockLocation, Product product);

  /**
   * Allow the creation of a detailed location line of a given product, product variant, and
   * tracking number in a given location.
   *
   * @param stockLocation A location
   * @param product A product
   * @param trackingNumber A tracking number
   * @return The created detailed stock location line
   */
  public StockLocationLine createDetailLocationLine(
      StockLocation stockLocation, Product product, TrackingNumber trackingNumber);

  /**
   * Allow to get the available qty of product in a given location.
   *
   * @param stockLocation
   * @param product
   * @return
   */
  public BigDecimal getAvailableQty(StockLocation stockLocation, Product product);

  /**
   * Allow to get the available qty of product for a given Tracking Number.
   *
   * @param stockLocation
   * @param trackingNumber
   * @return
   */
  public BigDecimal getTrackingNumberAvailableQty(
      StockLocation stockLocation, TrackingNumber trackingNumber);

  /**
   * For a given line, compute the future quantity of a stock location line from its current qty and
   * planned stock move lines with the same stock location and the same product.
   *
   * @param stockLocationLine a stock location line with a product and a stock location.
   * @return the future quantity of the stock location line.
   */
  BigDecimal computeFutureQty(StockLocationLine stockLocationLine) throws AxelorException;

  /**
   * Create a query to find stock location line of a product of a specific/all company and a
   * specific/all stock location
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  public String getStockLocationLineListForAProduct(
      Long productId, Long companyId, Long stockLocationId);

  /**
   * Create a query to find product's available qty of a specific/all company and a specific/all
   * stock location
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  public String getAvailableStockForAProduct(Long productId, Long companyId, Long stockLocationId);

  /**
   * Create a query to find product's requested reserved qty of a specific/all company and a
   * specific/all stock location
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  public String getRequestedReservedQtyForAProduct(
      Long productId, Long companyId, Long stockLocationId);

  /**
   * Update avgPrice in stock location line and save wap history in the line.
   *
   * @param stockLocationLine stock location line to updated.
   * @param wap weighted average price which will update the field avgPrice.
   * @deprecated Please use updateHistory this method will not create a proper history
   */
  @Deprecated
  void updateWap(StockLocationLine stockLocationLine, BigDecimal wap);

  /**
   * Update avgPrice in stock location line and save wap history in the line.
   *
   * @param stockLocationLine stock location line to updated.
   * @param wap weighted average price which will update the field avgPrice.
   * @param stockMoveLine the move line responsible for the WAP change.
   * @deprecated Please use updateHistory this method will not create a proper history
   */
  @Deprecated
  void updateWap(StockLocationLine stockLocationLine, BigDecimal wap, StockMoveLine stockMoveLine);

  /**
   * Same as {@link #updateWap(StockLocationLine, BigDecimal, StockMoveLine)} but date and origin
   * can be personalized.
   *
   * @param stockLocationLine
   * @param wap
   * @param stockMoveLine
   * @param date
   * @deprecated Please use updateHistory this method will not create a proper history
   */
  @Deprecated
  void updateWap(
      StockLocationLine stockLocationLine,
      BigDecimal wap,
      StockMoveLine stockMoveLine,
      LocalDate date,
      String origin);

  /**
   * Update stock location line history
   *
   * @param stockLocationLine must not be null
   * @param stockMoveLine can be null
   * @param dateT can be null but will be by default todayDate
   * @param origin can be null
   * @param typeSelect must not be null (see {@link StockLocationLineHistoryRepository})
   */
  void updateHistory(
      StockLocationLine stockLocationLine,
      StockMoveLine stockMoveLine,
      LocalDateTime dateT,
      String origin,
      String typeSelect);
}
