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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.FreightCarrierMode;
import com.axelor.apps.stock.db.Incoterm;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StockMoveService {

  /**
   * Generic method to create any stock move
   *
   * @param fromAddress
   * @param toAddress
   * @param company
   * @param clientPartner
   * @param fromStockLocation
   * @param toStockLocation
   * @param realDate
   * @param estimatedDate
   * @param description
   * @param shipmentMode
   * @param freightCarrierMode
   * @param carrierPartner
   * @param forwarderPartner
   * @param incoterm
   * @param typeSelect
   * @return
   * @throws AxelorException No Stock move sequence defined
   */
  public StockMove createStockMove(
      Address fromAddress,
      Address toAddress,
      Company company,
      Partner clientPartner,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      LocalDate realDate,
      LocalDate estimatedDate,
      String note,
      ShipmentMode shipmentMode,
      FreightCarrierMode freightCarrierMode,
      Partner carrierPartner,
      Partner forwarderPartner,
      Incoterm incoterm,
      int typeSelect)
      throws AxelorException;

  /**
   * Generic method to create any stock move for internal stock move (without partner information)
   *
   * @param clientPartner
   * @param shipmentMode
   * @param freightCarrierMode
   * @param carrierPartner
   * @param forwarderPartner
   * @param incoterm
   * @param fromAddress
   * @param toAddress
   * @param company
   * @param fromStockLocation
   * @param toStockLocation
   * @param realDate
   * @param estimatedDate
   * @param description
   * @param typeSelect
   * @return
   * @throws AxelorException No Stock move sequence defined
   */
  public StockMove createStockMove(
      Address fromAddress,
      Address toAddress,
      Company company,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      LocalDate realDate,
      LocalDate estimatedDate,
      String note,
      int typeSelect)
      throws AxelorException;

  public void validate(StockMove stockMove) throws AxelorException;

  public void goBackToDraft(StockMove stockMove) throws AxelorException;

  public void plan(StockMove stockMove) throws AxelorException;

  public String realize(StockMove stockMove) throws AxelorException;

  public String realize(StockMove stockMove, boolean check) throws AxelorException;

  public boolean mustBeSplit(List<StockMoveLine> stockMoveLineList);

  public Optional<StockMove> copyAndSplitStockMove(StockMove stockMove) throws AxelorException;

  public Optional<StockMove> copyAndSplitStockMove(
      StockMove stockMove, List<StockMoveLine> stockMoveLines) throws AxelorException;

  public Optional<StockMove> copyAndSplitStockMoveReverse(StockMove stockMove, boolean split)
      throws AxelorException;

  public Optional<StockMove> copyAndSplitStockMoveReverse(
      StockMove stockMove, List<StockMoveLine> stockMoveLines, boolean split)
      throws AxelorException;

  void cancel(StockMove stockMove) throws AxelorException;

  void cancel(StockMove stockMove, CancelReason cancelReason) throws AxelorException;

  @Transactional
  public Boolean splitStockMoveLinesUnit(List<StockMoveLine> stockMoveLines, BigDecimal splitQty);

  @Transactional
  public void splitStockMoveLinesSpecial(
      StockMove stockMove, List<StockMoveLine> list, BigDecimal splitQty);

  @Transactional
  public void copyQtyToRealQty(StockMove stockMove);

  @Transactional(rollbackOn = {Exception.class})
  public Optional<StockMove> generateReversion(StockMove stockMove) throws AxelorException;

  public StockMove splitInto2(
      StockMove originalStockMove, List<StockMoveLine> modifiedStockMoveLines)
      throws AxelorException;

  public List<Map<String, Object>> getStockPerDate(
      Long locationId, Long productId, LocalDate fromDate, LocalDate toDate);

  /**
   * Change conformity on each stock move line according to the stock move conformity.
   *
   * @param stockMove
   * @return
   */
  List<StockMoveLine> changeConformityStockMove(StockMove stockMove);

  /**
   * Change stock move conformity according to the conformity on each stock move line.
   *
   * @param stockMove
   * @return
   */
  Integer changeConformityStockMoveLine(StockMove stockMove);

  /**
   * Called from {@link com.axelor.apps.stock.web.StockMoveController#viewDirection}
   *
   * @param stockMove
   * @return the direction for the google map API
   */
  Map<String, Object> viewDirection(StockMove stockMove) throws AxelorException;

  /**
   * Print the given stock move.
   *
   * @param stockMove
   * @param lstSelectedMove
   * @param reportType true if we print a picking order
   * @return the link to the PDF file
   * @throws AxelorException
   */
  String printStockMove(StockMove stockMove, List<Integer> lstSelectedMove, String reportType)
      throws AxelorException;

  /**
   * Update fully spread over logistical forms flag on stock move.
   *
   * @param stockMove
   */
  void updateFullySpreadOverLogisticalFormsFlag(StockMove stockMove);

  void setAvailableStatus(StockMove stockMove);

  void checkExpirationDates(StockMove stockMove) throws AxelorException;

  /**
   * Update editDate of one Outgoing Stock Move
   *
   * @param stockMove
   * @param userType
   */
  void setPickingStockMoveEditDate(StockMove stockMove, String userType);

  /**
   * Update editDate of a list of Outgoing Stock Move
   *
   * @param ids
   * @param userType
   */
  void setPickingStockMovesEditDate(List<Long> ids, String userType);

  /**
   * Update stocks using saved stock move line list and current stock move line list. Then we save
   * current stock move line list, replacing the saved list.
   *
   * @param stockMove
   */
  void updateStocks(StockMove stockMove) throws AxelorException;

  void updateProductNetMass(StockMove stockMove) throws AxelorException;
}
