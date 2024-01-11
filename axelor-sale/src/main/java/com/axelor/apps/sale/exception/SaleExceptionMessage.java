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
package com.axelor.apps.sale.exception;

public final class SaleExceptionMessage {

  private SaleExceptionMessage() {}

  /** Sales Order Stock Move Service */
  public static final String SALES_ORDER_STOCK_MOVE_1 = /*$$(*/
      "Invoice by delivery impose that all sale order lines must have service or stockable product with provision from stock" /*)*/;

  /** Sales Order Service Impl */
  public static final String SALES_ORDER_1 = /*$$(*/
      "The company %s doesn't have any configured sequence for sale orders" /*)*/;

  public static final String SALES_ORDER_COMPLETED = /*$$(*/ "This sale order is completed." /*)*/;

  /** Sale Config Service */
  public static final String SALE_CONFIG_1 = /*$$(*/
      "%s : You must configure Sales module for company %s" /*)*/;

  /** Merge sale order */
  public static final String SALE_ORDER_MERGE_ERROR_CURRENCY = /*$$(*/
      "The currency is required and must be the same for all sale orders" /*)*/;

  public static final String SALE_ORDER_MERGE_ERROR_CLIENT_PARTNER = /*$$(*/
      "The client Partner is required and must be the same for all sale orders" /*)*/;
  public static final String SALE_ORDER_MERGE_ERROR_COMPANY = /*$$(*/
      "The company is required and must be the same for all sale orders" /*)*/;
  public static final String SALE_ORDER_MERGE_ERROR_FISCAL_POSITION = /*$$(*/
      "The fiscal position must be the same for all sale orders" /*)*/;
  public static final String SALE_ORDER_MERGE_ERROR_TAX_NUMBER = /*$$(*/
      "The tax number must be the same for all sale orders" /*)*/;
  public static final String SALE_ORDER_MERGE_LIST_EMPTY = /*$$(*/
      "List of sale orders to merge is empty" /*)*/;

  public static final String SALE_ORDER_PRINT = /*$$(*/
      "Please select the sale order(s) to print." /*)*/;
  public static final String SALE_ORDER_MISSING_PRINTING_SETTINGS = /*$$(*/
      "Please fill printing settings on sale order %s." /*)*/;

  /** Configurator creator */
  public static final String CONFIGURATOR_CREATOR_SCRIPT_ERROR = /*$$(*/
      "This script has errors, please see server logs for more details." /*)*/;

  public static final String CONFIGURATOR_CREATOR_FORMULA_TYPE_ERROR = /*$$(*/
      "This script returned value is of type %s, it should return a value of type %s instead." /*)*/;
  public static final String CONFIGURATOR_CREATOR_SCRIPT_WORKING = /*$$(*/
      "The syntax of the script is correct." /*)*/;

  /** Configurator Service */
  public static final String CONFIGURATOR_PRODUCT_MISSING_NAME = /*$$(*/
      "You must configure a script to fill the created product name." /*)*/;

  public static final String CONFIGURATOR_PRODUCT_MISSING_CODE = /*$$(*/
      "You must configure a script to fill the created product code." /*)*/;
  public static final String CONFIGURATOR_SALE_ORDER_LINE_MISSING_PRODUCT_NAME = /*$$(*/
      "You must configure a script to fill the product name in the created sale order line." /*)*/;

  public static final String CONFIGURATOR_ON_GENERATING_TYPE_ERROR = /*$$(*/
      "The field %s is of type %s, but the configured script returned value is of type %s." /*)*/;

  public static final String SALE_ORDER_EDIT_ORDER_NOTIFY = /*$$(*/
      "At least one sale order line has a stock move with availability request." /*)*/;

  public static final String SALE_ORDER_DISCOUNT_TOO_HIGH = /*$$(*/
      "There are lines with a discount superior to the maximal authorized discount." /*)*/;

  public static final String CONFIGURATOR_ONE_TO_MANY_WITHOUT_MAPPED_BY_UNSUPPORTED = /*$$(*/
      "Missing mapped by: unidirectional one-to-many are not supported by the configurator." /*)*/;

  public static final String COPY = /*$$(*/ "copy" /*)*/;

  public static final String SALE_ORDER_LINE_PRICING_NOT_APPLIED = /*$$(*/
      "You are using a product for which the '%s' pricing should be applied.</br>However, it could not be applied.</br>Please check your pricing if this does not seem normal." /*)*/;

  /** Sale Order Workflow Service * */
  public static final String SALE_ORDER_FINALIZE_QUOTATION_WRONG_STATUS = /*$$(*/
      "Can only finalize a drafted quotation." /*)*/;

  public static final String SALE_ORDER_CONFIRM_WRONG_STATUS = /*$$(*/
      "Can only confirm a finalized quotation." /*)*/;
  public static final String SALE_ORDER_COMPLETE_WRONG_STATUS = /*$$(*/
      "Can only complete a confirmed sale order." /*)*/;
  public static final String SALE_ORDER_CANCEL_WRONG_STATUS = /*$$(*/
      "Can only cancel a drafted or finalized sale order." /*)*/;

  public static final String OPPORTUNITY_PARTNER_MISSING = /*$$(*/
      "You must fill a partner for the opportunity %s." /*)*/;
}
