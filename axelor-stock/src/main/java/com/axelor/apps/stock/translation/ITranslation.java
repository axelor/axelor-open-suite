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
package com.axelor.apps.stock.translation;

public interface ITranslation {

  public static final String STOCK_APP_NAME = /*$$(*/ "value:Stock"; /*)*/

  public static final String ABC_ANALYSIS_STOCK_LOCATION = /*$$(*/
      "AbcAnalysis.stockLocation"; /*)*/

  public static final String PICKING_STOCK_MOVE_NOTE = /*$$(*/ "PickingStockMove.note"; /*)*/
  public static final String STOCK_ON_TIME_DELIVERIES = /*$$(*/ "OnTime Deliveries"; /*)*/

  String MANUAL_CORRECTION = /*$$(*/ "value:Manual correction"; /*)*/

  String MASS_STOCK_MOVE_NEED_CREATED = /*$$(*/ "Lines created successfully"; /*)*/
  String MASS_STOCK_MOVE_NEED_PICKED = /*$$(*/ "Picked"; /*)*/
  String MASS_STOCK_MOVE_NEED_TO_PICK = /*$$(*/ "To pick"; /*)*/
  String MASS_STOCK_MOVE_NEED_STORED = /*$$(*/ "Stored"; /*)*/
  String MASS_STOCK_MOVE_NEED_TO_STORE = /*$$(*/ "To store"; /*)*/

  public static final String REQUEST_COMPLETED = /*$$(*/ "Request completed" /*)*/;
  public static final String INVENTORY_LINE_UPDATED = /*$$(*/
      "Inventory line successfully updated" /*)*/;
  public static final String INVENTORY_UPDATED = /*$$(*/ "Inventory successfully updated" /*)*/;
  public static final String REAL_QTY_UPDATED = /*$$(*/ "Real qty updated;" /*)*/;
  public static final String REASON_UPDATED = /*$$(*/ "Reason updated;" /*)*/;
  public static final String STATUS_UPDATED = /*$$(*/ "Status updated;" /*)*/;
  public static final String COMMENTS_UPDATED = /*$$(*/ "Comments updated;" /*)*/;
  public static final String STOCK_MOVE_LINE_UPDATED = /*$$(*/ "Line successfully updated." /*)*/;
  public static final String STOCK_MOVE_LINE_SPLIT = /*$$(*/ "Line successfully split." /*)*/;
  public static final String STOCK_MOVE_LINE_ADDED_TO_STOCK_MOVE = /*$$(*/
      "Line successfully added to stock move with id %s" /*)*/;
  public static final String STOCK_MOVE_LINE_QUANTITY_AVAILABILITY = /*$$(*/
      "Stock move line quantity availability." /*)*/;
  public static final String STOCK_MOVE_REALIZED = /*$$(*/
      "Stock move with id %s successfully realized." /*)*/;
  public static final String STOCK_MOVE_UPDATED = /*$$(*/ "Successfully updated" /*)*/;
  public static final String UPDATE_LOCKER_FOR_PRODUCT = /*$$(*/
      "Update locker for product with id %s to %s" /*)*/;
}
