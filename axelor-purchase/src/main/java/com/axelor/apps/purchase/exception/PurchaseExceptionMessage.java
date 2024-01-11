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
package com.axelor.apps.purchase.exception;

public final class PurchaseExceptionMessage {

  private PurchaseExceptionMessage() {}

  public static final String PURCHASE_ORDER_LINE_TAX_LINE = /*$$(*/ "A tax line is missing" /*)*/;
  public static final String PURCHASE_ORDER_LINE_MIN_QTY = /*$$(*/
      "The minimum order quantity of %s to the supplier is not respected. Unit price might be different than the supplier catalog one." /*)*/;
  public static final String PURCHASE_ORDER_LINE_NO_SUPPLIER_CATALOG = /*$$(*/
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
  public static final String PURCHASE_ORDER_REQUEST_WRONG_STATUS = /*$$(*/
      "Can only request drafted purchase order." /*)*/;
  public static final String PURCHASE_ORDER_VALIDATE_WRONG_STATUS = /*$$(*/
      "Can only validate requested purchase order." /*)*/;
  public static final String PURCHASE_ORDER_FINISH_WRONG_STATUS = /*$$(*/
      "Can only finish validated purchase order." /*)*/;
  public static final String PURCHASE_ORDER_CANCEL_WRONG_STATUS = /*$$(*/
      "Can only cancel drafted, requested or validated purchase order." /*)*/;
  public static final String PURCHASE_ORDER_DRAFT_WRONG_STATUS = /*$$(*/
      "Can only return to draft from cancelled purchase order." /*)*/;

  /** Blocking supplier */
  public static final String SUPPLIER_BLOCKED = /*$$(*/ "This supplier is blocked:" /*)*/;

  /*
   * Purchase order printing
   */
  public static final String NO_PURCHASE_ORDER_SELECTED_FOR_PRINTING = /*$$(*/
      "Please select the purchase order(s) to print." /*)*/;
  public static final String PURCHASE_ORDER_MISSING_PRINTING_SETTINGS = /*$$(*/
      "Please fill printing settings on purchase order %s" /*)*/;
  public static final String PURCHASE_ORDERS_MISSING_PRINTING_SETTINGS = /*$$(*/
      "Please fill printing settings on following purchase orders: %s" /*)*/;

  public static final String PURCHASE_REQUEST_1 = /*$$(*/
      "There is no sequence set for the purchase requests for the company %s" /*)*/;
  public static final String PURCHASE_REQUEST_MISSING_SUPPLIER_USER = /*$$(*/
      "Please enter supplier for following purchase request : %s" /*)*/;
  public static final String PURCHASE_REQUEST_REQUEST_WRONG_STATUS = /*$$(*/
      "Can only request drafted purchase request." /*)*/;
  public static final String PURCHASE_REQUEST_ACCEPT_WRONG_STATUS = /*$$(*/
      "Can only accept requested purchase request." /*)*/;
  public static final String PURCHASE_REQUEST_PURCHASE_WRONG_STATUS = /*$$(*/
      "Can only purchase accepted purchase request." /*)*/;
  public static final String PURCHASE_REQUEST_REFUSE_WRONG_STATUS = /*$$(*/
      "Can only refuse requested purchase request." /*)*/;
  public static final String PURCHASE_REQUEST_CANCEL_WRONG_STATUS = /*$$(*/
      "Can not cancel already canceled purchase request." /*)*/;
  public static final String PURCHASE_REQUEST_DRAFT_WRONG_STATUS = /*$$(*/
      "Can only return to draft from canceled purchase request." /*)*/;

  public static final String DIFFERENT_SUPPLIER = /*$$(*/
      "The supplier of the purchase order is different from the default supplier of the product." /*)*/;
}
