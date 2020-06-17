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
/** */
package com.axelor.apps.purchase.exception;

/** @author axelor */
public interface IExceptionMessage {

  static final String PURCHASE_ORDER_LINE_TAX_LINE = /*$$(*/ "A tax line is missing" /*)*/;
  static final String PURCHASE_ORDER_LINE_MIN_QTY = /*$$(*/
      "The minimum order quantity of %s to the supplier is not respected." /*)*/;
  static final String PURCHASE_ORDER_LINE_NO_SUPPLIER_CATALOG = /*$$(*/
      "This product is not available from the supplier." /*)*/;

  /** Purchase order service impl */
  public static final String PURCHASE_ORDER_1 = /*$$(*/
      "The company %s doesn't have any configured sequence for the purchase orders" /*)*/;

  /** Purchase config service */
  public static final String PURCHASE_CONFIG_1 = /*$$(*/
      "You must configure Purchase module for the company %s" /*)*/;

  /** Merge purchase order */
  public static final String PURCHASE_ORDER_MERGE_ERROR_CURRENCY = /*$$(*/
      "The currency is required and must be the same for all purchase orders" /*)*/;

  public static final String PURCHASE_ORDER_MERGE_ERROR_SUPPLIER_PARTNER = /*$$(*/
      "The supplier Partner is required and must be the same for all purchase orders" /*)*/;
  public static final String PURCHASE_ORDER_MERGE_ERROR_COMPANY = /*$$(*/
      "The company is required and must be the same for all purchase orders" /*)*/;
  public static final String PURCHASE_ORDER_MERGE_ERROR_TRADING_NAME = /*$$(*/
      "The trading name must be the same for all purchase orders" /*)*/;

  /** Blocking supplier */
  String SUPPLIER_BLOCKED = /*$$(*/ "This supplier is blocked:" /*)*/;

  /*
   * Purchase order printing
   */
  String NO_PURCHASE_ORDER_SELECTED_FOR_PRINTING = /*$$(*/
      "Please select the purchase order(s) to print." /*)*/;
  String PURCHASE_ORDER_MISSING_PRINTING_SETTINGS = /*$$(*/
      "Please fill printing settings on purchase order %s" /*)*/;
  String PURCHASE_ORDERS_MISSING_PRINTING_SETTINGS = /*$$(*/
      "Please fill printing settings on following purchase orders: %s" /*)*/;

  public static final String PURCHASE_REQUEST_1 = /*$$(*/
      "There is no sequence set for the purchase requests for the company %s" /*)*/;
  public static final String PURCHASE_REQUEST_MISSING_SUPPLIER_USER = /*$$(*/
      "Please enter supplier for following purchase request : %s" /*)*/;
}
