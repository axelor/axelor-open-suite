/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.exception;

public final class StockExceptionMessage {

  private StockExceptionMessage() {}

  /** Inventory service and controller */
  public static final String INVENTORY_1 = /*$$(*/ "You must select a stock location" /*)*/;

  public static final String INVENTORY_2 = /*$$(*/
      "There's no configured sequence for inventory for company" /*)*/;
  public static final String INVENTORY_3 = /*$$(*/
      "An error occurred while importing the file data. Please contact your application administrator to check Traceback logs." /*)*/;
  public static final String INVENTORY_4 = /*$$(*/
      "An error occurred while importing the file data, product not found with code :" /*)*/;
  public static final String INVENTORY_5 = /*$$(*/
      "There is currently no such file in the specified folder or the folder may not exists." /*)*/;
  public static final String INVENTORY_6 = /*$$(*/ "Company missing for stock location %s" /*)*/;
  public static final String INVENTORY_7 = /*$$(*/ "Incorrect product in inventory line" /*)*/;
  public static final String INVENTORY_8 = /*$$(*/ "File %s successfully imported." /*)*/;
  public static final String INVENTORY_9 = /*$$(*/ "There's no product in stock location." /*)*/;
  public static final String INVENTORY_10 = /*$$(*/
      "Inventory's lines' list has been filled." /*)*/;
  public static final String INVENTORY_11 = /*$$(*/ "No inventory lines has been created." /*)*/;
  public static final String INVENTORY_12 = /*$$(*/
      "An error occurred while importing the file data, there are multiple products with code :" /*)*/;
  public static final String INVENTORY_3_LINE_LENGHT = /*$$(*/ "Line length too big" /*)*/;
  public static final String INVENTORY_3_REAL_QUANTITY = /*$$(*/ "Real quantity problem" /*)*/;
  public static final String INVENTORY_3_CURRENT_QUANTITY = /*$$(*/
      "Current quantity problem" /*)*/;
  public static final String INVENTORY_3_DATA_NULL_OR_EMPTY = /*$$(*/ "Data is null or empty" /*)*/;
  public static final String INVENTORY_PLAN_WRONG_STATUS = /*$$(*/
      "Can only plan a draft inventory." /*)*/;
  public static final String INVENTORY_START_WRONG_STATUS = /*$$(*/
      "Can only start a draft inventory." /*)*/;
  public static final String INVENTORY_COMPLETE_WRONG_STATUS = /*$$(*/
      "Can only complete a started inventory." /*)*/;
  public static final String INVENTORY_VALIDATE_WRONG_STATUS = /*$$(*/
      "Cannot validate an inventory that is not completed." /*)*/;
  public static final String INVENTORY_CANCEL_WRONG_STATUS = /*$$(*/
      "Can only cancel a validated inventory." /*)*/;
  public static final String INVENTORY_DRAFT_WRONG_STATUS = /*$$(*/
      "Can only return to draft if cancelled." /*)*/;
  public static final String INVENTORY_VALIDATE_INVENTORY_LINE_LIST = /*$$(*/
      "Please fill all inventory lines" /*)*/;

  public static final String INVENTORY_LINE_STOCK_LOCATION_MISSING = /*$$(*/
      "Stock location is missing for these lines : %s" /*)*/;

  /** Stock Location Line Service Impl */
  public static final String LOCATION_LINE_1 = /*$$(*/
      "Product's stocks %s (%s) are not in sufficient quantity to realize the delivery" /*)*/;

  public static final String LOCATION_LINE_2 = /*$$(*/
      "Product's stocks %s (%s), tracking number %s are not in sufficient quantity to realize the delivery" /*)*/;
  public static final String LOCATION_LINE_3 = /*$$(*/
      "Product's stocks %s (%s) exceeds maximum stock rules." /*)*/;

  public static final String LOCATION_LINE_MISSING_UNIT = /*$$(*/
      "Please fill unit for the stock location line in %s for the product %s." /*)*/;

  public static final String DETAIL_LOCATION_LINE_MISSING_UNIT = /*$$(*/
      "Please fill unit for the stock location line with tracking number %s in %s for the product %s." /*)*/;

  /** Stock Move Service and Controller */
  public static final String STOCK_MOVE_1 = /*$$(*/
      "There's no configured sequence for stock's intern moves for the company %s" /*)*/;

  public static final String STOCK_MOVE_2 = /*$$(*/
      "There's no configured sequence for stock's receptions for the company %s" /*)*/;
  public static final String STOCK_MOVE_3 = /*$$(*/
      "There's no configured sequence for stock's delivery for the company %s" /*)*/;
  public static final String STOCK_MOVE_4 = /*$$(*/ "Stock's movement's type undefined" /*)*/;
  public static final String STOCK_MOVE_5 = /*$$(*/
      "There's no source stock location selected for the stock's movement %s" /*)*/;
  public static final String STOCK_MOVE_6 = /*$$(*/
      "There's no destination stock location selected for the stock's movement %s" /*)*/;
  public static final String STOCK_MOVE_7 = /*$$(*/ "Partial stock move (From" /*)*/;
  public static final String STOCK_MOVE_8 = /*$$(*/ "%s Reverse stock move (From %s)" /*)*/;
  public static final String STOCK_MOVE_9 = /*$$(*/
      "A partial stock move has been generated (%s)" /*)*/;
  public static final String STOCK_MOVE_10 = /*$$(*/
      "Please select the StockMove(s) to print." /*)*/;
  public static final String STOCK_MOVE_11 = /*$$(*/ "Company address is empty." /*)*/;
  public static final String STOCK_MOVE_12 = /*$$(*/
      "Feature currently not available with Open Street Maps." /*)*/;
  public static final String STOCK_MOVE_13 = /*$$(*/ "<B>%s or %s</B> not found" /*)*/;
  public static final String STOCK_MOVE_14 = /*$$(*/ "No move lines to split" /*)*/;
  public static final String STOCK_MOVE_15 = /*$$(*/ "Please select lines to split" /*)*/;
  public static final String STOCK_MOVE_16 = /*$$(*/ "Please enter a valid split quantity" /*)*/;
  public static final String STOCK_MOVE_17 = /*$$(*/
      "Must set mass unit in stock configuration for customs." /*)*/;
  public static final String STOCK_MOVE_18 = /*$$(*/
      "All storable products used in DEB must have net mass and mass unit information for customs." /*)*/;
  public static final String STOCK_MOVE_19 = /*$$(*/
      "Can't realize this stock move because of the ongoing inventory %s." /*)*/;
  public static final String STOCK_MOVE_PLAN_WRONG_STATUS = /*$$(*/
      "Can only plan a drafted stock move." /*)*/;
  public static final String STOCK_MOVE_REALIZATION_WRONG_STATUS = /*$$(*/
      "Cannot realize a stock move that is not planned." /*)*/;
  public static final String STOCK_MOVE_CANCEL_WRONG_STATUS = /*$$(*/
      "Can only cancel a planned or realized stock move." /*)*/;
  public static final String STOCK_MOVE_PLANNED_NOT_DELETED = /*$$(*/
      "Can't delete a planned stock move" /*)*/;
  public static final String STOCK_MOVE_REALIZED_NOT_DELETED = /*$$(*/
      "Can't delete a realized stock move" /*)*/;
  public static final String STOCK_MOVE_SPLIT_NOT_GENERATED = /*$$(*/
      "No new stock move was generated" /*)*/;
  public static final String STOCK_MOVE_INCOMING_PARTIAL_GENERATED = /*$$(*/
      "An incoming partial stock move has been generated (%s)" /*)*/;
  public static final String STOCK_MOVE_OUTGOING_PARTIAL_GENERATED = /*$$(*/
      "An outgoing partial stock move has been generated (%s)" /*)*/;
  public static final String STOCK_MOVE_MISSING_TEMPLATE = /*$$(*/
      "The template to send message on realization is missing." /*)*/;
  public static final String STOCK_MOVE_QTY_BY_TRACKING = /*$$(*/
      "The tracking number configuration quantity is equal to zero, it must be at least one." /*)*/;
  public static final String STOCK_MOVE_TOO_MANY_ITERATION = /*$$(*/
      "Too many iterations while trying to generate stock move line with tracking numbers." /*)*/;
  public static final String STOCK_MOVE_CANNOT_GO_BACK_TO_DRAFT = /*$$(*/
      "Cannot go back to draft status." /*)*/;

  /*
   * Stock Move printing
   */
  public static final String STOCK_MOVES_MISSING_PRINTING_SETTINGS = /*$$(*/
      "Please fill printing settings on following stock moves: %s" /*)*/;

  public static final String STOCK_MOVE_PRINT = /*$$(*/
      "Please select the stock move(s) to print" /*)*/;

  /** Tracking Number Service */
  public static final String TRACKING_NUMBER_1 = /*$$(*/
      "There's no configured sequence for tracking number for the product %s:%s" /*)*/;

  /** Stock Config Service */
  public static final String STOCK_CONFIG_1 = /*$$(*/
      "You must configure a Stock module for the company %s" /*)*/;

  public static final String STOCK_CONFIG_2 = /*$$(*/
      "You must configure an inventory virtual stock location for the company %s" /*)*/;
  public static final String STOCK_CONFIG_3 = /*$$(*/
      "You must configure a supplier virtual stock location for the company %s" /*)*/;
  public static final String STOCK_CONFIG_4 = /*$$(*/
      "You must configure a customer virtual stock location for the company %s" /*)*/;
  public static final String STOCK_CONFIG_RECEIPT = /*$$(*/
      "You must configure a default receipt stock location for the company %s" /*)*/;
  public static final String STOCK_CONFIG_PICKUP = /*$$(*/
      "You must configure a default pickup stock location for the company %s" /*)*/;
  public static final String STOCK_CONFIG_VIRTUAL_OUTSOURCE_STOCK_LOCATION = /*$$(*/
      "You must configure a default virtual outsource stock location for the company %s" /*)*/;
  public static final String PO_MISSING_DEFAULT_STOCK_LOCATION = /*$$(*/
      "Please add a quality control default stock location for company %s in the app stock configuration" /*)*/;

  /** Stock Location Controller */
  public static final String LOCATION_1 = /*$$(*/
      "There's already an existing storage, you must deactivate it first" /*)*/;

  public static final String LOCATION_2 = /*$$(*/
      "Please select the Stock Location(s) to print." /*)*/;

  public static final String STOCK_LOCATION_PRINT_WIZARD_TITLE = /*$$(*/
      "Select format to Export" /*)*/;

  /** Stock Move Line Service */
  public static final String STOCK_MOVE_LINE_MUST_FILL_CONFORMITY =
      /*$$(*/ "Please fill the conformity for the product(s) : %s" /*)*/;

  public static final String STOCK_MOVE_LINE_MUST_FILL_TRACKING_NUMBER =
      /*$$(*/ "Please fill the tracking number for the product(s) : %s" /*)*/;

  public static final String STOCK_MOVE_LINE_EXPIRED_PRODUCTS = /*$$(*/
      "Expired product(s): %s" /*)*/;

  public static final String STOCK_MOVE_LINE_MISSING_QUANTITY = /*$$(*/
      "Please indicate a new quantity." /*)*/;

  public static final String MISSING_PRODUCT_MASS_UNIT = /*$$(*/
      "Please configure mass units for this product packing : %s" /*)*/;

  public static final String STOCK_CONFIGURATION_MISSING = /*$$(*/
      "Configuration is missing in stock configuration to see financial data" /*)*/;

  /** Partner Product Quality Rating Service */
  public static final String PARTNER_PRODUCT_QUALITY_RATING_MISSING_PARTNER = /*$$(*/
      "Partner is missing." /*)*/;

  /*
   * Logistical form
   */
  public static final String LOGISTICAL_FORM_MISSING_SEQUENCE = /*$$(*/
      "Missing logistical form sequence for company %s" /*)*/;
  public static final String LOGISTICAL_FORM_PARTNER_MISMATCH = /*$$(*/
      "Partner mismatch: %s" /*)*/;
  public static final String LOGISTICAL_FORM_LINE_INVALID_DIMENSIONS = /*$$(*/
      "Invalid dimensions on packing line No. %d" /*)*/;
  public static final String LOGISTICAL_FORM_LINE_REQUIRED_TYPE = /*$$(*/
      "Type is required on line %d." /*)*/;
  public static final String LOGISTICAL_FORM_LINE_REQUIRED_STOCK_MOVE_LINE = /*$$(*/
      "Stock move line is required on line %d." /*)*/;
  public static final String LOGISTICAL_FORM_LINE_REQUIRED_QUANTITY = /*$$(*/
      "Quantity is required on line %d." /*)*/;
  public static final String LOGISTICAL_FORM_LINES_INCONSISTENT_QUANTITY = /*$$(*/
      "Total quantity for %s: %s (expected: %s)" /*)*/;
  public static final String LOGISTICAL_FORM_LINES_EMPTY_PARCEL = /*$$(*/
      "Parcel %d is empty." /*)*/;
  public static final String LOGISTICAL_FORM_LINES_EMPTY_PALLET = /*$$(*/
      "Pallet %d is empty." /*)*/;
  public static final String LOGISTICAL_FORM_LINES_ORPHAN_DETAIL = /*$$(*/
      "Detail line(s) not inside a parcel/pallet" /*)*/;
  public static final String LOGISTICAL_FORM_UNKNOWN_ACCOUNT_SELECTION = /*$$(*/
      "Unknown account selection" /*)*/;

  public static final String LOGISTICAL_FORM_MISSING_TEMPLATE = /*$$(*/
      "The template to send message on save is missing." /*)*/;

  public static final String LOGISTICAL_FORM_INVALID_DIMENSIONS = /*$$(*/
      "Invalid field dimensions" /*)*/;

  public static final String LOGISTICAL_FORM_CARRIER_VALIDATE_WRONG_STATUS = /*$$(*/
      "Can only be validated if provisioned" /*)*/;
  public static final String LOGISTICAL_FORM_COLLECT_WRONG_STATUS = /*$$(*/
      "Can only be collected if validated" /*)*/;
  public static final String LOGISTICAL_FORM_PROVISION_WRONG_STATUS = /*$$(*/
      "Can only return to provision if was validated or collected" /*)*/;

  public static final String CANCEL_REASON_MISSING = /*$$(*/
      "A cancel reason must be selected" /*)*/;
  public static final String CANCEL_REASON_BAD_TYPE = /*$$(*/
      "The type of cancel reason doesn't match with stock move" /*)*/;
  public static final String STOCK_LOCATION_UNIT_NULL = /*$$(*/
      "The unit is missing on a stock location line" /*)*/;
  ;
  /*
   * Declaration of exchanges
   */
  public static final String DECLARATION_OF_EXCHANGES_ECONOMIC_AREA_MISSING = /*$$(*/
      "No economic area is configured for %s." /*)*/;
  public static final String DECLARATION_OF_EXCHANGES_ECONOMIC_AREA_UNSUPPORTED = /*$$(*/
      "Declaration of exchanges for %s is not supported." /*)*/;
  public static final String DECLARATION_OF_EXCHANGES_ECONOMIC_AREA_MISSING_IN_APP_STOCK = /*$$(*/
      "Please set an economic are in AppStock." /*)*/;

  public static final String TRACK_NUMBER_WIZARD_TITLE_1 = /*$$(*/
      "Create new tracking numbers" /*)*/;
  public static final String TRACK_NUMBER_WIZARD_TITLE_2 = /*$$(*/ "Select tracking numbers" /*)*/;
  public static final String TRACK_NUMBER_WIZARD_NO_RECORD_ADDED_ERROR = /*$$(*/
      "No Tracking Numbers Added" /*)*/;

  public static final String TRACK_NUMBER_DATE_MISSING = /*$$(*/
      "Please fill estimated delivery date for product %s from %s" /*)*/;

  /** Stock correction service and controller */
  public static final String STOCK_CORRECTION_1 = /*$$(*/
      "Incorrect product for stock correction" /*)*/;

  public static final String STOCK_CORRECTION_2 = /*$$(*/
      "No stock correction needed, current quantity in stock equals real quantity." /*)*/;
  public static final String STOCK_CORRECTION_VALIDATE_WRONG_STATUS = /*$$(*/
      "Can only validate a drafted stock correction." /*)*/;

  public static final String INVENTORY_PRODUCT_TRACKING_NUMBER_ERROR = /*$$(*/
      "There is more than one line for same product with same tracking number." /*)*/;

  public static final String REST_STOCK_MOVE_LINE_STOCK_LOCATION_REQUIRED = /*$$(*/
      "Error: %s field is required and can not be null" /*)*/;
  public static final String REST_STOCK_MOVE_LINE_STOCK_LOCATIONS_REQUIRED = /*$$(*/
      "Error: %s and %s fields are required and can not be null" /*)*/;

  /** Stock move merging controller and service */
  public static final String STOCK_MOVE_NO_LINE_SELECTED = /*$$(*/
      "Please select at least 2 stock moves" /*)*/;

  public static final String STOCK_MOVE_MERGE_ERROR = /*$$(*/
      "Your merge doesn't respect this/these condition(s) : %s" /*)*/;
  public static final String STOCK_MOVE_MERGE_ERROR_COMPANY = /*$$(*/ "Same company" /*)*/;
  public static final String STOCK_MOVE_MERGE_ERROR_TRADING_NAME = /*$$(*/
      "Same trading name" /*)*/;
  public static final String STOCK_MOVE_MERGE_ERROR_PARTNER = /*$$(*/ "Same partner" /*)*/;
  public static final String STOCK_MOVE_MERGE_ERROR_FROM_AND_TO_STOCK_LOCATION = /*$$(*/
      "Same from stock location and to stock location" /*)*/;
  public static final String STOCK_MOVE_MERGE_ERROR_STATUS = /*$$(*/
      "Status must be planned or draft" /*)*/;
  public static final String STOCK_MOVE_MERGE_ERROR_MANUF_ORDER = /*$$(*/ "Same manuf order" /*)*/;
  public static final String STOCK_MOVE_DIFF_SHIPMENT_FIELDS = /*$$(*/
      "Fields linked to the shipment are differents, the value of the oldest will be kept." /*)*/;

  public static final String STOCK_MOVE_TRACKING_NUMBER_DIMENSION_NOT_COMPUTED = /*$$(*/
      "No dimension could be computed, please make sure that you have the necessary field to compute at least one field" /*)*/;
  public static final String STOCK_MOVE_TRACKING_NUMBER_DIMENSION_EXPECTED_DECIMAL_RESULT = /*$$(*/
      "Expected BigDecimal as result during the computation of field %s, please fix the corresponding formula." /*)*/;
  public static final String STOCK_MOVE_TRACKING_NUMBER_PARENT_MAXIMUM_ITERATION_REACHED = /*$$(*/
      "Maximum iteration for looking tracking numbers parent has been reached. Please check any circular parent dependencies." /*)*/;

  public static final String SPLIT_MOVE_LINE_WRONG_STATUS = /*$$(*/
      "You can only split line in draft or planned stock move." /*)*/;

  public static final String SPLIT_MOVE_LINE_WRONG_TYPE = /*$$(*/
      "You can only split line in incoming or outgoing stock move." /*)*/;

  public static final String SPLIT_MOVE_LINE_WRONG_REAL_QTY = /*$$(*/
      "Real quantity must not be equal to 0 and should be less than expected quantity." /*)*/;

  public static final String STOCK_MOVE_MASS_MOVED_QTY_GREATER_THAN_CURRENT_QTY = /*$$(*/
      "Moved qty is greater than current quantity (%s)" /*)*/;

  public static final String STOCK_MOVE_MASS_PRODUCT_NO_AVAILABLE_IN_STOCKLOCATION_SOURCE = /*$$(*/
      "The product is not available in the stock location source indicated (%s)" /*)*/;

  public static final String STOCK_MOVE_MASS_MOVED_QUANTITY_IS_ZERO_OR_LESS = /*$$(*/
      "Moved quantity is equal to 0 or less (%s)" /*)*/;
  public static final String STOCK_MOVE_MASS_STOCK_MOVE_LINE_CANT_DELETE = /*$$(*/
      "Can't delete a line linked to a realized stock move line." /*)*/;

  public static final String STOCK_MOVE_MASS_FACTORY_UNKNOWN_OBJECT = /*$$(*/
      "Unknown object, can not get service" /*)*/;

  public static final String STOCK_MOVE_MASS_PRODUCT_NO_STOCK_LOCATION_SOURCE_AVAILABLE = /*$$(*/
      "The product %s is missing a stock location (source or dest). Please make sure that the cart stock location and stock location in the product is filled." /*)*/;

  public static final String STOCK_MOVE_MASS_ALREADY_STORED_PRODUCT = /*$$(*/
      "The product %s have already been stored" /*)*/;

  public static final String MASS_STOCK_MOVE_NO_SEQUENCE = /*$$(*/
      "There's no configured sequence for mass stock move for the company %s" /*)*/;

  public static final String LOCATIONS_ARE_EMPTY = /*$$(*/
      "Source and destination locations are empty" /*)*/;

  public static final String STOCK_MOVE_MASS_COULD_NOT_CREATE_PICKED_PRODUCT_FROM_NEED = /*$$(*/
      "Impossible to create a picked product for product %s, no stock found for company %s" /*)*/;

  public static final String STOCK_MOVE_MASS_NO_FROM_STOCK_LOCATION_SELECTED = /*$$(*/
      "No stock location has been selected." /*)*/;
}
