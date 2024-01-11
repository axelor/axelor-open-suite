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
package com.axelor.apps.supplychain.exception;

public final class SupplychainExceptionMessage {

  private SupplychainExceptionMessage() {}

  /** Purchase order Invoice Service and controller */
  public static final String PO_INVOICE_1 = /*$$(*/
      "Please, select a currency for the order %s" /*)*/;

  public static final String PO_INVOICE_2 = /*$$(*/ "Invoice created" /*)*/;

  /** Purchase order Service */
  public static final String PURCHASE_ORDER_1 = /*$$(*/
      "%s please configure a virtual supplier stock location for the company %s" /*)*/;

  public static final String PURCHASE_ORDER_2 = /*$$(*/
      "Error : you have exceeded the budget %s for this period" /*)*/;

  public static final String PO_INVOICE_TOO_MUCH_INVOICED = /*$$(*/
      "The purchase order %s invoiced amount cannot be greater than its total amount." /*)*/;
  public static final String PURCHASE_ORDER_RETURN_TO_VALIDATE_WRONG_STATUS = /*$$(*/
      "Can only return to validated from finished purchase order." /*)*/;

  public static final String PURCHASE_ORDER_TRADING_NAME_MISSING = /*$$(*/
      "The purchase order trading name is missing." /*)*/;

  /** Sale order Invoice Service */
  public static final String SO_INVOICE_6 = /*$$(*/
      "Please, select a currency for the order %s" /*)*/;

  public static final String SO_INVOICE_NO_LINES_SELECTED = /*$$(*/
      "There are no lines to invoice" /*)*/;
  public static final String SO_INVOICE_NO_TIMETABLES_SELECTED = /*$$(*/
      "There are no selected timetables to invoice" /*)*/;
  public static final String SO_INVOICE_QTY_MAX = /*$$(*/
      "The quantity to invoice is greater than the quantity in the sale order" /*)*/;
  public static final String SO_INVOICE_AMOUNT_MAX = /*$$(*/
      "The amount to invoice is superior than the amount in the sale order" /*)*/;
  public static final String SO_INVOICE_MISSING_INVOICING_PRODUCT = /*$$(*/
      "Please configure the sale order invoicing product" /*)*/;
  public static final String SO_INVOICE_MISSING_ADVANCE_PAYMENT_PRODUCT = /*$$(*/
      "Please configure the advance payment product" /*)*/;
  public static final String SO_INVOICE_MISSING_ADVANCE_PAYMENT_ACCOUNT = /*$$(*/
      "You must configure an advance payment account for the company %s" /*)*/;
  public static final String PO_INVOICE_MISSING_SUPPLIER_ADVANCE_PAYMENT_ACCOUNT = /*$$(*/
      "You must configure an supplier advance payment account for the company %s" /*)*/;
  public static final String SO_INVOICE_TOO_MUCH_INVOICED = /*$$(*/
      "The sale order %s invoiced amount cannot be greater than its total amount." /*)*/;
  public static final String SO_INVOICE_GENERATE_ALL_INVOICES = /*$$(*/
      "All invoices have been generated for this sale order." /*)*/;

  /** Sale order Purchase Service */
  public static final String SO_PURCHASE_1 = /*$$(*/
      "Please, select a supplier for the line %s" /*)*/;

  public static final String SO_LINE_PURCHASE_AT_LEAST_ONE = /*$$(*/
      "At least one sale order line must be selected" /*)*/;

  /** Stock Move Invoice Service */
  public static final String STOCK_MOVE_INVOICE_1 = /*$$(*/
      "Incorrect product in the stock move %s" /*)*/;

  public static final String STOCK_MOVE_MULTI_INVOICE_CURRENCY = /*$$(*/
      "The currency is required and must be the same for all sale orders" /*)*/;
  public static final String STOCK_MOVE_MULTI_INVOICE_CLIENT_PARTNER = /*$$(*/
      "The client is required and must be the same for all sale orders" /*)*/;
  public static final String STOCK_MOVE_MULTI_INVOICE_SUPPLIER_PARTNER = /*$$(*/
      "The supplier is required and must be the same for all purchase orders" /*)*/;
  public static final String STOCK_MOVE_MULTI_INVOICE_COMPANY_SO = /*$$(*/
      "The company is required and must be the same for all sale orders" /*)*/;
  public static final String STOCK_MOVE_MULTI_INVOICE_COMPANY_PO = /*$$(*/
      "The company is required and must be the same for all purchase orders" /*)*/;
  public static final String STOCK_MOVE_MULTI_INVOICE_TRADING_NAME_SO = /*$$(*/
      "The trading name must be the same for all sale orders." /*)*/;
  public static final String STOCK_MOVE_MULTI_INVOICE_TRADING_NAME_PO = /*$$(*/
      "The trading name must be the same for all purchase orders." /*)*/;
  public static final String STOCK_MOVE_MULTI_FISCAL_POSITION_PO = /*$$(*/
      "The fiscal position must be the same for all purchase orders" /*)*/;
  public static final String STOCK_MOVE_MULTI_FISCAL_POSITION_SO = /*$$(*/
      "The fiscal position must be the same for all sale orders" /*)*/;
  public static final String STOCK_MOVE_MULTI_INVOICE_IN_ATI = /*$$(*/
      "Unit prices in A.T.I and in W.T. can't be mix" /*)*/;
  public static final String STOCK_MOVE_MULTI_INVOICE_INCOTERM = /*$$(*/
      "The incoterm must be the same for all sale orders." /*)*/;
  public static final String STOCK_MOVE_NO_INVOICE_GENERATED = /*$$(*/
      "No invoice was generated" /*)*/;
  public static final String STOCK_MOVE_GENERATE_INVOICE = /*$$(*/
      "The invoice for the stock move %s can't be generated because of this following error : %s" /*)*/;
  public static final String OUTGOING_STOCK_MOVE_INVOICE_EXISTS = /*$$(*/
      "An invoice not canceled already exists for the outgoing stock move %s" /*)*/;
  public static final String INCOMING_STOCK_MOVE_INVOICE_EXISTS = /*$$(*/
      "An invoice not canceled already exists for the incoming stock move %s" /*)*/;
  public static final String STOCK_MOVE_AVAILABILITY_REQUEST_NOT_UPDATABLE = /*$$(*/
      "Please uncheck picking order edited box from this stock move from Cust. Shipment to prepare menu entry." /*)*/;
  public static final String STOCK_MOVE_CANCEL_WRONG_STATUS_ERROR = /*$$(*/
      "Can't cancel a stock move that is partially or totally invoiced." /*)*/;
  public static final String STOCK_MOVE_CANCEL_WRONG_INVOICE_STATUS_ALERT = /*$$(*/
      "An invoice at status draft or validated is linked to the stock move %s. Please contact the invoicing/accounting department before any changes." /*)*/;

  /** Stock move line service */
  public static final String STOCK_MOVE_MISSING_SALE_ORDER = /*$$(*/
      "Missing link to sale order line (from sale order id = %s) for stock move line %s" /*)*/;

  public static final String STOCK_MOVE_MISSING_PURCHASE_ORDER = /*$$(*/
      "Missing purchase order with id %s for stock move line %s" /*)*/;

  /** Batch Invoicing */
  public static final String BATCH_INVOICING_1 = /*$$(*/
      "Subscription invoice generation report :" /*)*/;

  public static final String BATCH_INVOICING_2 = /*$$(*/ "Order(s) processed" /*)*/;

  /** Batch Outgoing stock move invoicing */
  public static final String BATCH_OUTGOING_STOCK_MOVE_INVOICING_REPORT = /*$$(*/
      "Outgoing stock move invoicing report:" /*)*/;

  public static final String BATCH_OUTGOING_STOCK_MOVE_INVOICING_DONE_SINGULAR = /*$$(*/
      "%d outgoing stock move processed successfully," /*)*/;
  public static final String BATCH_OUTGOING_STOCK_MOVE_INVOICING_DONE_PLURAL = /*$$(*/
      "%d outgoing stock moves processed successfully," /*)*/;

  /** Batch Order invoicing */
  public static final String BATCH_ORDER_INVOICING_REPORT = /*$$(*/ "Order invoicing report:" /*)*/;

  public static final String BATCH_ORDER_INVOICING_DONE_SINGULAR = /*$$(*/
      "%d order invoiced successfully," /*)*/;
  public static final String BATCH_ORDER_INVOICING_DONE_PLURAL = /*$$(*/
      "%d orders invoiced successfully," /*)*/;

  /** Batch update stock history */
  public static final String BATCH_UPDATE_STOCK_HISTORY_1 = /*$$(*/
      "Batch update stock history :" /*)*/;

  public static final String BATCH_UPDATE_STOCK_HISTORY_2 = /*$$(*/ "Stock history updated" /*)*/;

  /** Mrp Line Service */
  public static final String MRP_LINE_1 = /*$$(*/
      "No default supplier is defined for the product %s" /*)*/;

  public static final String MRP_MISSING_STOCK_LOCATION_VALID = /*$$(*/
      "No stock location valid. Please uncheck the chosen stock location 'is not in MRP'." /*)*/;

  public static final String MRP_NO_PRODUCT = /*$$(*/
      "Please select an element to run calculation" /*)*/;

  public static final String MRP_NO_PRODUCT_UNIT = /*$$(*/ "Please fill unit for product %s" /*)*/;

  public static final String MRP_NO_PRODUCT_ID = /*$$(*/
      "The component %s - %s not referenced in the product list selected for the MRP or in the BOM route should be added on the MRP." /*)*/;

  public static final String MRP_TOO_MANY_ITERATIONS = /*$$(*/
      "The process was stopped because the computation is stuck in an infinite loop. This error can be caused by a configuration error." /*)*/;
  public static final String MRP_ALREADY_STARTED = /*$$(*/
      "Mrp calculation is already on going." /*)*/;

  public static final String MRP_CANNOT_PROCESS_ONGOING = /*$$(*/
      "Could not process MRP %s since its computation is already ongoing." /*)*/;

  public static final String MRP_NOT_FOUND_WITH_SEQ = /*$$(*/
      "Could not find MRP with sequence %s." /*)*/;

  public static final String MRP_SCHEDULER_SEQ_MISSING = /*$$(*/ "MRP sequence is missing." /*)*/;

  public static final String MRP_GENERATE_PROPOSAL_NO_LINE_SELECTED = /*$$(*/
      "Please select at least one line to generate a proposal." /*)*/;

  public static final String MRP_GENERATE_PROPOSAL_NO_POSSIBLE_LINE = /*$$(*/
      "No proposal can be generated from these lines." /*)*/;

  //  Mrp Forecast
  public static final String MRP_FORECAST_CONFIRM_WRONG_STATUS = /*$$(*/
      "Can only be confirmed from drafted forecast." /*)*/;
  public static final String MRP_FORECAST_CANCEL_WRONG_STATUS = /*$$(*/
      "Can only be cancelled from drafted or confirmed forecast." /*)*/;

  /** Sale order Stock Service Implement */
  public static final String SO_NO_DELIVERY_STOCK_MOVE_TO_GENERATE = /*$$(*/
      "No delivery stock move to generate for this sale order" /*)*/;

  public static final String SO_ACTIVE_DELIVERY_STOCK_MOVE_ALREADY_EXISTS = /*$$(*/
      "An active stock move (%s) already exists for the sale order %s." /*)*/;
  public static final String SO_CANT_REMOVED_DELIVERED_LINE = /*$$(*/
      "Can't remove delivered detail line %s." /*)*/;
  public static final String SO_CANT_DECREASE_QTY_ON_DELIVERED_LINE = /*$$(*/
      "Quantity cannot be lower than already delivered quantity on detail line %s." /*)*/;
  public static final String SO_MISSING_STOCK_LOCATION = /*$$(*/
      "Stock location is missing for the sale order %s." /*)*/;

  public static final String PO_NO_DELIVERY_STOCK_MOVE_TO_GENERATE = /*$$(*/
      "No delivery stock move to generate for this purchase order" /*)*/;

  public static final String RESERVATION_SALE_ORDER_DATE_CONFIG_INCORRECT_VALUE = /*$$(*/
      "Please configure a correct value for the sale order date used for reservation." /*)*/;

  /** Purchase Order Stock Service Implement */
  public static final String PO_MISSING_STOCK_LOCATION = /*$$(*/
      "Stock location is missing for the purchase order %s." /*)*/;

  /** Timetable Controller */
  public static final String TIMETABLE_INVOICE_ALREADY_GENERATED = /*$$(*/
      "The invoice has already been generated." /*)*/;

  public static final String TIMETABLE_SALE_ORDER_NOT_CONFIRMED = /*$$(*/
      "Please confirm the sale order before invoicing." /*)*/;

  /** Ventilate State Service */
  public static final String VENTILATE_STATE_MISSING_ADVANCE_ACCOUNT = /*$$(*/
      "Please configure the advance payment account for the company %s" /*)*/;

  /** Supply Chain Config */
  public static final String SUPPLY_CHAIN_CONFIG = /*$$(*/
      "You must configure a Supply chain module for the company %s" /*)*/;

  public static final String SUPPLYCHAIN_MISSING_CANCEL_REASON_ON_CHANGING_SALE_ORDER = /*$$(*/
      "You must configure a cancel reason on changing sale order in app supplychain." /*)*/;

  /** Subscription invoice */
  public static final String TOTAL_SUBSCRIPTION_INVOICE_GENERATED = /*$$(*/
      "Total subscription invoice(s) generated: %s" /*)*/;

  public static final String SUBSCRIPTION_INVOICE_GENERATION_ERROR = /*$$(*/
      "Error generating subscription invoice(s): '%s'" /*)*/;

  /** Interco Service */
  public static final String INVOICE_MISSING_TYPE = /*$$(*/ "Invoice %s type is not filled." /*)*/;

  /** Stock location line service supplychain impl */
  public static final String LOCATION_LINE_RESERVED_QTY = /*$$(*/
      "Not enough quantity are available for reservation for product %s (%s)" /*)*/;

  /** Reserved qty service */
  public static final String LOCATION_LINE_NOT_ENOUGH_AVAILABLE_QTY = /*$$(*/
      "This operation cannot be performed. Available stock for product %s: %s, stock needed: %s. Please deallocate." /*)*/;

  public static final String SALE_ORDER_LINE_NO_STOCK_MOVE = /*$$(*/
      "Please generate a stock move for this sale order before modifying allocated quantity." /*)*/;

  public static final String SALE_ORDER_LINE_REQUEST_QTY_NEGATIVE = /*$$(*/
      "You cannot request reservation with a negative quantity." /*)*/;

  public static final String SALE_ORDER_LINE_RESERVATION_QTY_NEGATIVE = /*$$(*/
      "Please do not enter negative quantity for reservation." /*)*/;

  public static final String SALE_ORDER_LINE_REQUESTED_QTY_TOO_HIGH = /*$$(*/
      "The requested quantity must not be greater than the quantity in the stock move line %s." /*)*/;

  public static final String SALE_ORDER_LINE_ALLOCATED_QTY_TOO_HIGH = /*$$(*/
      "The allocated quantity must not be greater than the quantity in the stock move line %s." /*)*/;

  public static final String SALE_ORDER_LINE_QTY_NOT_AVAILABLE = /*$$(*/
      "This quantity is not available in stock." /*)*/;

  public static final String SALE_ORDER_LINE_AVAILABILITY_REQUEST = /*$$(*/
      "The reservation for an availability requested stock move cannot be lowered." /*)*/;

  public static final String SALE_ORDER_LINE_REQUESTED_QTY_TOO_LOW = /*$$(*/
      "The requested quantity must be greater than the already delivered quantity." /*)*/;

  public static final String SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED = /*$$(*/
      "This product is not stock managed." /*)*/;

  /** Account config supplychain service */
  public static final String FORECASTED_INVOICE_CUSTOMER_ACCOUNT = /*$$(*/
      "You must configure a forecasted invoiced customer account for the company %s" /*)*/;

  public static final String FORECASTED_INVOICE_SUPPLIER_ACCOUNT = /*$$(*/
      "You must configure a forecasted invoiced supplier account for the company %s" /*)*/;

  /** Accounting cut off service */
  public static final String ACCOUNTING_CUT_OFF_GENERATION_REPORT = /*$$(*/
      "Accounting cut off generation report :" /*)*/;

  public static final String ACCOUNTING_CUT_OFF_STOCK_MOVE_PROCESSED = /*$$(*/
      "Stock move(s) processed" /*)*/;

  public static final String SALE_ORDER_STOCK_MOVE_CREATED = /*$$(*/
      "Stock move %s has been created for this sale order" /*)*/;

  public static final String SUPPLYCHAIN_MRP_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for MRP" /*)*/;

  public static final String STOCK_MOVE_VERIFY_PRODUCT_STOCK_ERROR = /*$$(*/
      "Product stock for %s is not enough for availability request" /*)*/;

  public static final String SALE_ORDER_ANALYTIC_DISTRIBUTION_ERROR = /*$$(*/
      "There is no analytic distribution on %s sale order line" /*)*/;

  public static final String PURCHASE_ORDER_ANALYTIC_DISTRIBUTION_ERROR = /*$$(*/
      "There is no analytic distribution on %s purchase order line" /*)*/;

  public static final String STOCK_MOVE_INVOICE_ERROR = /*$$(*/
      "Stock move has already been totally invoiced." /*)*/;

  public static final String STOCK_MOVE_INVOICE_QTY_INVONVERTIBLE_UNIT = /*$$(*/
      "The invoice's unit is different and inconvertible into the stock move's unit." /*)*/;

  public static final String STOCK_MOVE_PARTIAL_INVOICE_ERROR = /*$$(*/
      "Stock move %s has already been invoiced." /*)*/;

  public static final String STOCK_MOVE_INVOICE_QTY_MAX = /*$$(*/
      "It is not allowed to invoice a greater quantity %s than the delivered quantity %s. Pls check the invoice line with this product %s." /*)*/;

  public static final String SALE_ORDER_COMPLETE_MANUALLY = /*$$(*/
      "There is at least one draft or planned stock move for this sale order." /*)*/;

  public static final String ALLOCATED_STOCK_MOVE_LINE_DELETED_ERROR = /*$$(*/
      "It is not possible to delete a stock move line with allocated or reserved quantity." /*)*/;

  public static final String BLOCK_SPLIT_OUTGOING_STOCK_MOVE_LINES = /*$$(*/
      "Partial invoicing of outgoing stock move with tracking number activated is not supported." /*)*/;

  public static final String STOCK_MOVE_NO_LINES_TO_INVOICE = /*$$(*/
      "Please fill a quantity to invoice." /*)*/;

  public static final String TIMETABLE_PURCHASE_OREDR_NOT_VALIDATED = /*$$(*/
      "Please validate the purchase order before invoicing." /*)*/;

  public static final String PO_INVOICE_QTY_MAX = /*$$(*/
      "The quantity to invoice is greater than the quantity in the purchase order" /*)*/;

  public static final String SALE_ORDER_CLIENT_PARTNER_EXCEEDED_CREDIT = /*$$(*/
      "%s blocked : maximal accepted credit exceeded for %s." /*)*/;

  public static final String SALE_ORDER_BANK_DETAILS_MISSING = /*$$(*/
      "%s : The advance payment generation failed. Please configure the bank details for the company %s and the associated payment mode %s either on the payment mode side or this Sale order %s record (hidden field)." /*)*/;

  public static final String CUSTOMER_HAS_BLOCKED_ACCOUNT = /*$$(*/
      "The customer account is blocked because he has late payments." /*)*/;

  public static final String SALE_ORDER_BACK_TO_CONFIRMED_WRONG_STATUS = /*$$(*/
      "Can only go back to confirmed if completed." /*)*/;

  /*
   * MRP Service
   */

  public static final String MRP_ERROR_WHILE_COMPUTATION = /*$$(*/
      "Error during the computation of MRP %s" /*)*/;

  public static final String MRP_FINISHED_MESSAGE_SUBJECT = /*$$(*/
      "MRP n°%s is now finished" /*)*/;
  public static final String MRP_FINISHED_MESSAGE_BODY = /*$$(*/
      "The execution of MRP n°%s is now finished, you can click above to see the results." /*)*/;

  public static final String MRP_STOCK_HISTORY_FIELD_SELECT_MISSING = /*$$(*/
      "Field fieldSelect is null for %s" /*)*/;

  public static final String CUT_OFF_BATCH_NO_LINE = /*$$(*/
      "You must select at least one line to validate" /*)*/;

  public static final String CUT_OFF_BATCH_NO_PARTNER_ACCOUNT = /*$$(*/
      "No partner account was found for company %s." /*)*/;
  public static final String SALE_ORDER_MERGE_ERROR_INVOICED_PARTNER = /*$$(*/
      "The invoiced partner must be the same for all sale orders" /*)*/;
  public static final String SALE_ORDER_MERGE_ERROR_DELIVERED_PARTNER = /*$$(*/
      "The delivered partner must be the same for all sale orders" /*)*/;
  public static final String SALE_ORDER_MERGE_ERROR_INCOTERM = /*$$(*/
      "The incoterm must be the same for all sale orders" /*)*/;

  public static final String MISSING_FORECASTED_INV_CUST_ACCOUNT = /*$$(*/
      "Please select a forecasted invoice customer account in the accounting batch" /*)*/;
  public static final String MISSING_FORECASTED_INV_SUPP_ACCOUNT = /*$$(*/
      "Please select a forecasted invoice supplier account in the accounting batch" /*)*/;

  public static final String STOCK_MOVE_LINES_ASSOCIATED_WITH_INVOICE_LINES_CANNOT_SPLIT = /*$$(*/
      "The following stock move lines are already associated with invoice lines and can not be splitted: %s" /*)*/;
}
