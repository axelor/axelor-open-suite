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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

public interface StockMoveLineService {

  public static final int TYPE_NULL = 0;
  public static final int TYPE_SALES = 1;
  public static final int TYPE_PURCHASES = 2;
  public static final int TYPE_OUT_PRODUCTIONS = 3;
  public static final int TYPE_IN_PRODUCTIONS = 4;
  public static final int TYPE_WASTE_PRODUCTIONS = 5;

  /**
   * Méthode générique permettant de créer une ligne de mouvement de stock en gérant les numéros de
   * suivi en fonction du type d'opération.
   *
   * @param product le produit
   * @param quantity la quantité
   * @param parent le StockMove parent
   * @param type 1 : Sales 2 : Purchases 3 : Productions
   * @return l'objet StockMoveLine
   * @throws AxelorException
   */
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal unitPrice,
      BigDecimal companyUnitPriceUntaxed,
      Unit unit,
      StockMove stockMove,
      int type,
      boolean taxed,
      BigDecimal taxRate)
      throws AxelorException;

  public void generateTrackingNumber(
      StockMoveLine stockMoveLine,
      TrackingNumberConfiguration trackingNumberConfiguration,
      Product product,
      BigDecimal qtyByTracking)
      throws AxelorException;

  /**
   * Allow the creation of a stock move line managing tracking numbers with operation type.
   *
   * @param product the line product
   * @param productName the line product name
   * @param description description of the line
   * @param quantity the line quantity
   * @param unitPriceUntaxed price untaxed of the line
   * @param unitPriceTaxed price taxed of the line
   * @param unit Unit of the line
   * @param stockMove parent stock move
   * @param trackingNumber tracking number used in the line
   * @return the created stock move line
   * @throws AxelorException
   */
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal unitPriceUntaxed,
      BigDecimal unitPriceTaxed,
      BigDecimal companyUnitPriceUntaxed,
      BigDecimal companyPurchasePrice,
      Unit unit,
      StockMove stockMove,
      TrackingNumber trackingNumber)
      throws AxelorException;

  public StockMoveLine assignOrGenerateTrackingNumber(
      StockMoveLine stockMoveLine,
      StockMove stockMove,
      Product product,
      TrackingNumberConfiguration trackingNumberConfiguration,
      int type)
      throws AxelorException;

  public void checkTrackingNumber(StockMove stockMove) throws AxelorException;

  public void assignTrackingNumber(
      StockMoveLine stockMoveLine, Product product, StockLocation stockLocation)
      throws AxelorException;

  public List<? extends StockLocationLine> getStockLocationLines(
      Product product, StockLocation stockLocation) throws AxelorException;

  public StockMoveLine splitStockMoveLine(
      StockMoveLine stockMoveLine, BigDecimal qty, TrackingNumber trackingNumber)
      throws AxelorException;

  public void updateLocations(
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      int fromStatus,
      int toStatus,
      List<StockMoveLine> stockMoveLineList,
      LocalDate lastFutureStockMoveDate,
      boolean realQty,
      boolean generateOrder)
      throws AxelorException;

  public void updateLocations(
      StockMoveLine stockMoveLine,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      Product product,
      BigDecimal qty,
      int fromStatus,
      int toStatus,
      LocalDate lastFutureStockMoveDate,
      TrackingNumber trackingNumber,
      boolean generateOrder)
      throws AxelorException;

  public void updateAveragePriceAndLocationLineHistory(
      StockLocation stockLocation,
      StockMoveLine stockMoveLine,
      int fromStatus,
      int toStatus,
      LocalDate date,
      String origin)
      throws AxelorException;

  /**
   * Check in the product if the stock move line needs to have a conformity selected.
   *
   * @param stockMoveLine
   * @param stockMove
   * @throws AxelorException if the stock move line needs to have a conformity selected and it is
   *     not selected.
   */
  public void checkConformitySelection(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException;

  /**
   * Check for all lines in the stock move if it needs to have a conformity selected.
   *
   * @param stockMove
   * @throws AxelorException if one or more stock move line needs to have a conformity selected and
   *     it is not selected.
   */
  public void checkConformitySelection(StockMove stockMove) throws AxelorException;

  /**
   * Check for warranty dates and expiration dates.
   *
   * @param stockMove
   * @throws AxelorException
   */
  public void checkExpirationDates(StockMove stockMove);

  /**
   * Return unit found in stock move line, or if the unit is empty, take the unit from the product.
   */
  Unit getStockUnit(StockMoveLine stockMoveLine);

  public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException;

  /**
   * Store customs code information on each stock move line from its product.
   *
   * @param stockMoveLineList List of StockMoveLines on which to operate
   * @throws AxelorException
   */
  public void storeCustomsCodes(List<StockMoveLine> stockMoveLineList) throws AxelorException;

  /**
   * Check whether a stock move line is fully spread over logistical form lines.
   *
   * @param stockMoveLine
   * @return
   */
  boolean computeFullySpreadOverLogisticalFormLinesFlag(StockMoveLine stockMoveLine);

  /**
   * Get the quantity spreadable over logistical form lines.
   *
   * @param stockMoveLine
   * @return
   */
  BigDecimal computeSpreadableQtyOverLogisticalFormLines(StockMoveLine stockMoveLine);

  /**
   * Get the quantity spreadable over logistical form lines. Take into account the lines from the
   * specified logistical form.
   *
   * @param stockMoveLine
   * @param logisticalForm
   * @return
   */
  BigDecimal computeSpreadableQtyOverLogisticalFormLines(
      StockMoveLine stockMoveLine, LogisticalForm logisticalForm);

  /**
   * Set product information.
   *
   * @param stockMove
   * @param stockMoveLine
   * @param company
   * @throws AxelorException
   */
  public void setProductInfo(StockMove stockMove, StockMoveLine stockMoveLine, Company company)
      throws AxelorException;

  /**
   * Check whether mass information is required.
   *
   * @param stockMove
   * @return
   */
  boolean checkMassesRequired(StockMove stockMove, StockMoveLine stockMoveLine);

  public void splitStockMoveLineByTrackingNumber(
      StockMoveLine stockMoveLine, List<LinkedHashMap<String, Object>> trackingNumbers);

  /**
   * set the available quantity of product in a given location.
   *
   * @param stockMoveLine
   * @param stockLocation
   * @return
   */
  public void updateAvailableQty(StockMoveLine stockMoveLine, StockLocation stockLocation);

  public String createDomainForProduct(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException;

  public void setAvailableStatus(StockMoveLine stockMoveLine);

  public List<TrackingNumber> getAvailableTrackingNumbers(
      StockMoveLine stockMoveLine, StockMove stockMove);

  /**
   * Fill realize avg price in stock move line. This method is called on realize, to save avg price
   * at the time of realization.
   *
   * @param stockMoveLine a stock move line being realized.
   */
  public void fillRealizeWapPrice(StockMoveLine stockMoveLine);

  /** Add a new line in a stock move (API AOS) */
  StockMoveLine createStockMoveLine(
      StockMove stockMove,
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal qty,
      BigDecimal realQty,
      Unit unit,
      Integer conformitySelect)
      throws AxelorException;

  /** To update realQty and conformity of a stock move line (API AOS) */
  void updateStockMoveLine(
      StockMoveLine stockMoveLine, BigDecimal realQty, Integer conformity, Unit unit)
      throws AxelorException;

  /**
   * Same as {@link #updateLocations(StockLocation, StockLocation, int, int, List, LocalDate,
   * boolean)} But instead of creating wap history at with today date,they will be created at date
   * specified and the origin specified. If origin is null, the behavior will be the same.
   *
   * @param fromStockLocation
   * @param toStockLocation
   * @param fromStatus
   * @param toStatus
   * @param stockMoveLineList
   * @param lastFutureStockMoveDate
   * @param realQty
   * @param date
   * @throws AxelorException
   */
  void updateLocations(
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      int fromStatus,
      int toStatus,
      List<StockMoveLine> stockMoveLineList,
      LocalDate lastFutureStockMoveDate,
      boolean realQty,
      LocalDate date,
      String origin,
      boolean generateOrder)
      throws AxelorException;

  BigDecimal computeNewAveragePriceLocationLine(
      StockLocationLine stockLocationLine, StockMoveLine stockMoveLine) throws AxelorException;

  /**
   * This method resets complety the stock move line. It does not remove the stock move line from
   * database.
   *
   * @param stockMoveLine
   * @return empty stock move line
   */
  public StockMoveLine resetStockMoveLine(StockMoveLine stockMoveLine);
}
