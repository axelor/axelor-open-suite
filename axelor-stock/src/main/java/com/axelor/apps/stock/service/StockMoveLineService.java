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
package com.axelor.apps.stock.service;

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
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
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
   * Méthode générique permettant de créer une ligne de mouvement de stock
   *
   * @param product
   * @param quantity
   * @param unit
   * @param price
   * @param stockMove
   * @param trackingNumber
   * @return
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
      Unit unit,
      StockMove stockMove,
      TrackingNumber trackingNumber)
      throws AxelorException;

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
      boolean realQty)
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
      BigDecimal reservedQty)
      throws AxelorException;

  public void updateAveragePriceLocationLine(
      StockLocation stockLocation, StockMoveLine stockMoveLine, int toStatus);

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
  public void checkExpirationDates(StockMove stockMove) throws AxelorException;

  public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException;

  /**
   * Store customs code information on each stock move line from its product.
   *
   * @param stockMoveLineList List of StockMoveLines on which to operate
   */
  public void storeCustomsCodes(List<StockMoveLine> stockMoveLineList);

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
}
