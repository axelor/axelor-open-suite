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
package com.axelor.apps.base.exceptions;

public final class BaseExceptionMessage {

  private BaseExceptionMessage() {}

  public static final String EXCEPTION = /*$$(*/ "Warning !" /*)*/;

  public static final String NOT_IMPLEMENTED_METHOD = /*$$(*/ "Not implemented yet!" /*)*/;

  public static final String BIRT_EXTERNAL_REPORT_NO_URL = /*$$(*/
      "Impossible to generate report, url toward Birt viewer is not correctly configured (%s)" /*)*/;

  public static final String RECORD_UNSAVED = /*$$(*/ "Unsaved record" /*)*/;
  public static final String RECORD_NONE_SELECTED = /*$$(*/
      "Please select at least one record." /*)*/;

  /** Currency service and controller */
  public static final String CURRENCY_1 = /*$$(*/
      "No currency conversion found from '%s' to '%s' for date %s" /*)*/;

  public static final String SEQUENCE_PREFIX = /*$$(*/
      "Sequence prefix cannot start with the draft prefix '%s'." /*)*/;

  public static final String CURRENCY_2 = /*$$(*/
      "The currency exchange rate from '%s' to '%s' for date %s must be different from zero." /*)*/;
  public static final String CURRENCY_3 = /*$$(*/
      "WARNING : For the specified currencies %s/%s, please first close the current open conversion period starting %3$tm/%3$td/%3$tY (by filling the end date) to then create a new one. Periods dates shouldn't overlap." /*)*/;
  public static final String CURRENCY_4 = /*$$(*/
      "The end date has to be greater than or equal to the start date." /*)*/;
  public static final String CURRENCY_5 = /*$$(*/
      "Both currencies must be saved before currency rate apply" /*)*/;
  public static final String CURRENCY_6 = /*$$(*/
      "Currency conversion webservice not working" /*)*/;
  public static final String CURRENCY_7 = /*$$(*/
      "Currency conversion rate not found over the past 7 days for the currency %s to %s. Please input the date and rate manually." /*)*/;
  public static final String CURRENCY_8 = /*$$(*/
      "The webservice URL to retrieve exchange rate is not configured. Please select the method to retrieve exchange rates in the App Base configuration > Tab Interfaces > Panel Webservices" /*)*/;
  public static final String CURRENCY_9 = /*$$(*/
      "WARNING : Process to retrieve exchange rate didn't work due to incorrect value in 'Currency Ws URL' in AppBase. Please contact support team for further investigation." /*)*/;
  public static final String CURRENCY_10 = /*$$(*/
      "There was no exchange rate found for %1$tm/%1$td/%1$tY. The retrieved exchange rate was the applicable rate at %2$tm/%2$td/%2$tY" /*)*/;
  public static final String CURRENCY_11 = /*$$(*/
      "WARNING : For the specified currencies %s/%s, the input fromDate is overlapping with an already existing conversion record. Period dates shouldn't overlap and are set on an included-included basis e.g. [From Date - To Date]." /*)*/;

  public static final String COMPANY_CURRENCY = /*$$(*/
      "%s : Please, configure a currency for the company %s" /*)*/;

  /** Unit conversion service */
  public static final String UNIT_CONVERSION_1 = /*$$(*/
      "Please configure unit conversion from '%s' to '%s'." /*)*/;

  public static final String UNIT_CONVERSION_2 = /*$$(*/ "Start unit cannot be void." /*)*/;

  public static final String UNIT_CONVERSION_3 = /*$$(*/
      "Conversion impossible because of no unit." /*)*/;

  public static final String UNIT_CONVERSION_4 = /*$$(*/ "End unit cannot be void." /*)*/;

  public static final String COEFFICIENT_SHOULD_NOT_BE_ZERO = /*$$(*/
      "The coefficient for unit conversion from %s to %s should not be 0." /*)*/;

  public static final String CURRENCY_CONVERSION_1 = /*$$(*/
      "WARNING : Please close the current conversion period before creating new one" /*)*/;
  public static final String CURRENCY_CONVERSION_2 = /*$$(*/
      "WARNING : To Date must be after or equals to From Date" /*)*/;

  /** Account management service */
  public static final String ACCOUNT_MANAGEMENT_2 = /*$$(*/ "No tax found for product %s" /*)*/;

  public static final String ACCOUNT_MANAGEMENT_3 = /*$$(*/
      "Tax configuration is missing for Product: %s (company: %s)" /*)*/;

  /** Period service */
  public static final String PERIOD_1 = /*$$(*/
      "No period found or it has been closed for the company %s for the date %s" /*)*/;

  public static final String PERIOD_2 = /*$$(*/ "Period closed" /*)*/;
  public static final String PERIOD_3 = /*$$(*/ "Too much iterations." /*)*/;
  public static final String PAY_PERIOD_CLOSED = /*$$(*/
      "Warning : the pay period %s is closed." /*)*/;

  public static final String PERIOD_CLOSING_MESSAGE = /*$$(*/ "Period %s is now closed" /*)*/;
  public static final String PERIOD_CLOSING_EXCEPTION_MESSAGE = /*$$(*/
      "Error when closing period %s" /*)*/;

  public static final String PERIOD_CLOSED_AND_NO_PERMISSIONS =
      /*$$(*/ "This period is closed and you do not have the necessary permissions to create entries" /*)*/;

  /** Abstract batch */
  public static final String ABSTRACT_BATCH_1 = /*$$(*/ "This batch is not runnable!" /*)*/;

  public static final String ABSTRACT_BATCH_2 = /*$$(*/ "Nested batch execution!" /*)*/;
  public static final String ABSTRACT_BATCH_REPORT = /*$$(*/ "Batch report:" /*)*/;
  public static final String ABSTRACT_BATCH_DONE_SINGULAR = /*$$(*/
      "%d record processed successfully," /*)*/;
  public static final String ABSTRACT_BATCH_DONE_PLURAL = /*$$(*/
      "%d records processed successfully," /*)*/;
  public static final String ABSTRACT_BATCH_ANOMALY_SINGULAR = /*$$(*/ "%d anomaly." /*)*/;
  public static final String ABSTRACT_BATCH_ANOMALY_PLURAL = /*$$(*/ "%d anomalies." /*)*/;

  public static final String ABSTRACT_BATCH_FINISHED_SUBJECT = /*$$(*/
      "Batch %s is now finished" /*)*/;
  public static final String ABSTRACT_BATCH_FINISHED_DEFAULT_MESSAGE = /*$$(*/
      "Batch execution is now finished" /*)*/;
  public static final String ABSTRACT_BATCH_MESSAGE_ON_EXCEPTION = /*$$(*/
      "Error happened during batch execution" /*)*/;

  /** Indicator generator grouping service */
  public static final String INDICATOR_GENERATOR_GROUPING_1 = /*$$(*/
      "Error : no export path has been set" /*)*/;

  public static final String INDICATOR_GENERATOR_GROUPING_2 = /*$$(*/
      "Error : no code has been set" /*)*/;
  public static final String INDICATOR_GENERATOR_GROUPING_3 = /*$$(*/
      "Error while creating the file" /*)*/;
  public static final String INDICATOR_GENERATOR_GROUPING_4 = /*$$(*/ "Result exported" /*)*/;

  /** Indicator generator service */
  public static final String INDICATOR_GENERATOR_1 = /*$$(*/
      "Error : a request has to be set for the indicatior generator %s" /*)*/;

  public static final String INDICATOR_GENERATOR_2 = /*$$(*/
      "Error : incorrect request for the indicatior generator %s" /*)*/;
  public static final String INDICATOR_GENERATOR_3 = /*$$(*/ "Request performed" /*)*/;

  /** Base batch service */
  public static final String BASE_BATCH_1 = /*$$(*/ "Unknown action %s for the %s treatment" /*)*/;

  public static final String BASE_BATCH_2 = /*$$(*/ "Batch %s unknown" /*)*/;
  public static final String BASE_BATCH_3 = /*$$(*/ "* %s anomaly(ies)" /*)*/;

  /** Product service */
  public static final String PRODUCT_NO_SEQUENCE = /*$$(*/
      "There is no configured sequence for product" /*)*/;

  public static final String CATEGORY_NO_SEQUENCE = /*$$(*/
      "There is no configured sequence for the category" /*)*/;

  public static final String APP_BASE_NO_SEQUENCE =
      "There is no configured product sequence in the app base config";

  public static final String SEQUENCE_ALREADY_EXISTS =
      /*$$(*/ "The generated sequence %s already exists. Please fix the configuration of sequence %s." /*)*/;

  /** Importer */
  public static final String IMPORTER_1 = /*$$(*/ "Error : Mapping file is unreachable." /*)*/;

  public static final String IMPORTER_2 = /*$$(*/ "Error : Data file is unreachable." /*)*/;
  public static final String IMPORTER_3 = /*$$(*/ "Error : Mapping file is not found." /*)*/;

  /** Importer Listener */
  public static final String IMPORTER_LISTERNER_1 = /*$$(*/ "Total :" /*)*/;

  public static final String IMPORTER_LISTERNER_2 = /*$$(*/ "- Succeeded :" /*)*/;
  public static final String IMPORTER_LISTERNER_3 = /*$$(*/ "Generated anomalies :" /*)*/;
  public static final String IMPORTER_LISTERNER_4 = /*$$(*/
      "The line cannot be imported (import : %s)" /*)*/;
  public static final String IMPORTER_LISTERNER_5 = /*$$(*/ "- Not null :" /*)*/;

  /** Template message service base impl */
  public static final String TEMPLATE_MESSAGE_BASE_1 = /*$$(*/
      "%s : Path to Birt template is incorrect" /*)*/;

  public static final String TEMPLATE_MESSAGE_BASE_2 = /*$$(*/
      "Unable to generate Birt report file" /*)*/;

  public static final String FILE_NOT_FOUND_IN_STANDARD_APPLICATION = /*$$(*/
      "%s was not found in the standard application." /*)*/;

  /** Tax service */
  public static final String TAX_1 = /*$$(*/ "Please enter a tax version for the tax %s" /*)*/;

  public static final String TAX_2 = /*$$(*/ "Tax is missing" /*)*/;

  public static final String TAX_DATE_MISSING = /*$$(*/
      "There is no date to determine which rate to take for the tax. Please define a tax active version for : %s." /*)*/;

  /** Template rule service */
  public static final String TEMPLATE_RULE_1 = /*$$(*/ "Bean is not an instance of" /*)*/;

  /** Sequence service */
  public static final String SEQUENCE_NOT_SAVED_RECORD = /*$$(*/
      "Can't generate draft sequence number on an unsaved record." /*)*/;

  public static final String SEQUENCE_PATTERN_LENGTH_NOT_VALID = /*$$(*/
      "The pattern length should be equal to padding ." /*)*/;

  public static final String SEQUENCE_LENGTH_NOT_VALID = /*$$(*/
      "Total sequence length must be less than 15 characters." /*)*/;

  public static final String SEQUENCE_TYPE_UNHANDLED = /*$$(*/
      "The sequence type '%s' is not handled." /*)*/;

  public static final String SEQUENCE_LETTERS_TYPE_IS_NULL = /*$$(*/
      "The sequence letter type can't be null." /*)*/;

  public static final String SEQUENCE_LETTERS_TYPE_UNHANDLED = /*$$(*/
      "The sequence letter type '%s' is not handled." /*)*/;

  /** Address controller */
  public static final String ADDRESS_1 = /*$$(*/ "OK" /*)*/;

  public static final String ADDRESS_2 = /*$$(*/
      "Service unavailable, please contact a administrator" /*)*/;
  public static final String ADDRESS_3 = /*$$(*/
      "There is no matching address in the QAS base" /*)*/;
  public static final String ADDRESS_4 = /*$$(*/ "NA" /*)*/;
  public static final String ADDRESS_5 = /*$$(*/ "<B>%s</B> not found" /*)*/;
  public static final String ADDRESS_6 = /*$$(*/
      "Feature currently not available with Open Street Maps." /*)*/;
  public static final String ADDRESS_7 = /*$$(*/
      "Current user's active company address is not set" /*)*/;
  public static final String ADDRESS_8 = /*$$(*/
      "You can select only one default invoicing address." /*)*/;
  public static final String ADDRESS_9 = /*$$(*/
      "You can select only one default delivery address." /*)*/;
  public static final String ADDRESS_CANNOT_BE_NULL = "Address cannot be null.";

  /** Bank details controller */
  public static final String BANK_DETAILS_1 = /*$$(*/
      "The entered IBAN code is not valid . <br> Either the code doesn't respect the norm, or the format you have entered is not correct. It has to be without any blank space, as the following : <br> FR0000000000000000000000000" /*)*/;

  public static final String BANK_DETAILS_2 = /*$$(*/
      "At least one iban code you have entered for this partner is not valid. Here is the list of invalid codes : %s" /*)*/;

  /** General controller */
  public static final String GENERAL_1 = /*$$(*/ "No duplicate records found" /*)*/;

  public static final String GENERAL_2 = /*$$(*/ "Duplicate records" /*)*/;
  public static final String GENERAL_3 = /*$$(*/
      "Please select key fields to check duplicate" /*)*/;
  public static final String GENERAL_4 = /*$$(*/
      "Attachment directory OR Application source does not exist" /*)*/;
  public static final String GENERAL_5 = /*$$(*/ "Export Object" /*)*/;
  public static final String GENERAL_6 = /*$$(*/ "Connection successful" /*)*/;
  public static final String GENERAL_7 = /*$$(*/ "Error in Connection" /*)*/;
  public static final String GENERAL_8 = /*$$(*/
      "Duplicate finder field '%s' is not found inside model '%s'" /*)*/;
  public static final String GENERAL_9 = /*$$(*/
      "Invalid duplicate finder field '%s'. Field type ManyToMany or OneToMany is not supported for duplicate check" /*)*/;
  public static final String GENERAL_10 = /*$$(*/ "No duplicate finder field configured." /*)*/;
  public static final String GENERAL_11 = /*$$(*/ "Please select original object." /*)*/;

  /** Messsage controller */
  public static final String MESSAGE_1 = /*$$(*/
      "Error in print. Please check report configuration and print setting." /*)*/;

  /** Partner controller */
  public static final String PARTNER_1 = /*$$(*/ "There is no sequence set for the partners" /*)*/;

  public static final String PARTNER_2 = /*$$(*/
      "%s SIRET Number required. Please configure SIRET Number for partner %s" /*)*/;
  public static final String PARTNER_3 = /*$$(*/
      "Can’t convert into an individual partner from scratch." /*)*/;
  public static final String PARTNER_NOT_FOUND = /*$$(*/ "Partner not found" /*)*/;
  public static final String PARTNER_EMAIL_EXIST = /*$$(*/
      "Email address already linked with another partner" /*)*/;

  public static final String PARTNER_INVALID_REGISTRATION_CODE = /*$$(*/
      "Registration code is invalid." /*)*/;

  /** Product controller */
  public static final String PRODUCT_1 = /*$$(*/ "Variants generated" /*)*/;

  public static final String PRODUCT_2 = /*$$(*/ "Prices updated" /*)*/;
  public static final String PRODUCT_NO_ACTIVE_COMPANY = /*$$(*/
      "No active company for this user, please define an active company." /*)*/;

  public static final String PRODUCT_COMPANY_NO_PRODUCT = /*$$(*/
      "Attempted to get field '%s' for unspecified product." /*)*/;

  public static final String PRODUCT_COMPANY_NO_FIELD = /*$$(*/
      "Attempted to get a field from product '%s' but forgot to specify which field." /*)*/;

  /** Calendar */
  public static final String CALENDAR_NOT_VALID = /*$$(*/ "Calendar configuration not valid" /*)*/;

  public static final String IMPORT_CALENDAR = /*$$(*/ "Import calendar" /*)*/;

  public static final String CALENDAR_NO_EVENTS_FOR_SYNC_ERROR = /*$$(*/
      "Calendars are empty, there is no event to synchronize." /*)*/;

  /** Price list */
  public static final String PRICE_LIST_DATE_WRONG_ORDER = /*$$(*/
      "The end date is before the begin date." /*)*/;

  public static final String PARTNER_PRICE_LIST_DATE_INCONSISTENT = /*$$(*/
      "The price list %s will still be active when the price list %s will become active." /*)*/;

  /** Advanced export */
  public static final String ADVANCED_EXPORT_1 = /*$$(*/ "Please select fields for export." /*)*/;

  public static final String ADVANCED_EXPORT_2 = /*$$(*/ "There is no records to export." /*)*/;
  public static final String ADVANCED_EXPORT_3 = /*$$(*/
      "Warning : Exported maximum export limit records." /*)*/;
  public static final String ADVANCED_EXPORT_4 = /*$$(*/
      "Please select export object or export format." /*)*/;

  /** Barcode Generator Service */
  public static final String BARCODE_GENERATOR_1 = /*$$(*/
      "Invalid serial number '%s' for '%s' barcode type.It must be of %d digits only." /*)*/;

  public static final String BARCODE_GENERATOR_2 = /*$$(*/
      "Invalid serial number '%s' for '%s' barcode type.It must be digits only with even number length." /*)*/;
  public static final String BARCODE_GENERATOR_3 = /*$$(*/
      "Invalid serial number '%s' for '%s' barcode type.It must be digits only" /*)*/;
  public static final String BARCODE_GENERATOR_4 = /*$$(*/
      "Invalid Serial Number '%s' for '%s' barcode type.Alphabets must be in uppercase only" /*)*/;
  public static final String BARCODE_GENERATOR_5 = /*$$(*/
      "Invalid Serial Number '%s' for '%s' barcode type.Its length limit must be greater than %d and less than %d" /*)*/;
  public static final String BARCODE_GENERATOR_6 = /*$$(*/
      "Invalid Serial Number '%s' for '%s' barcode type.It must be alphanumeric" /*)*/;
  public static final String BARCODE_GENERATOR_7 = /*$$(*/
      "Invalid Serial Number '%s' for '%s' barcode type.Its Length must be %d" /*)*/;
  public static final String BARCODE_GENERATOR_8 = /*$$(*/
      "Invalid Serial Number '%s' for '%s' barcode type.It must be only number or only alphabets" /*)*/;
  public static final String BARCODE_GENERATOR_9 = /*$$(*/ "Barcode format not supported" /*)*/;

  public static final String MAP_RESPONSE_ERROR = /*$$(*/ "Response error from map API: %s" /*)*/;
  ;
  public static final String MAP_GOOGLE_MAPS_API_KEY_MISSING = /*$$(*/
      "Google Maps API key is missing in configuration." /*)*/;

  /** Weekly planning service */
  public static final String WEEKLY_PLANNING_1 = /*$$(*/ "Invalid times %s morning" /*)*/;

  public static final String WEEKLY_PLANNING_2 = /*$$(*/
      "Invalid times on %s between morning and afternoon" /*)*/;
  public static final String WEEKLY_PLANNING_3 = /*$$(*/ "Invalid times %s afternoon" /*)*/;
  public static final String WEEKLY_PLANNING_4 = /*$$(*/
      "Some times are null and should not on %s" /*)*/;

  /*
   * User service
   */
  public static final String USER_CODE_ALREADY_EXISTS = /*$$(*/
      "A user with this login already exists." /*)*/;
  public static final String USER_PATTERN_MISMATCH_ACCES_RESTRICTION = /*$$(*/
      "Password must have at least 8 characters with at least three of these four types: lowercase, uppercase, digit, special." /*)*/;
  public static final String USER_PATTERN_MISMATCH_CUSTOM = /*$$(*/
      "Password doesn't match with configured pattern." /*)*/;
  public static final String USER_CODE_LENGTH_SHOULD_BE_GREATER_THAN_2 = /*$$(*/
      "Code length should be greater than 2." /*)*/;

  /** Convert demo data file */
  public static final String DUPLICATE_CSV_FILE_NAME_EXISTS = /*$$(*/
      "Please remove duplicate csv file name from excel file." /*)*/;

  public static final String CSV_FILE_NAME_NOT_EXISTS = /*$$(*/
      "Please provide valid csv file name." /*)*/;
  public static final String EXCEL_FILE_FORMAT_ERROR = /*$$(*/
      "Improper format of excel file." /*)*/;
  public static final String VALIDATE_FILE_TYPE = /*$$(*/ "Please import only excel file." /*)*/;

  public static final String TIMER_IS_NOT_STOPPED = /*$$(*/
      "You can't start a timer that has already started" /*)*/;
  public static final String TIMER_IS_NOT_STARTED = /*$$(*/
      "You can't stop a timer that hasn't been started" /*)*/;

  /** write to CSV from excel sheet */
  public static final String INVALID_HEADER = /*$$(*/ "Header is not valid." /*)*/;

  /** Import demo data from excel */
  public static final String MODULE = /*$$(*/ "Module" /*)*/;

  public static final String MODULE_NOT_EXIST = /*$$(*/ "%s module does not exist." /*)*/;
  public static final String DATA_FILE = /*$$(*/ "Data file" /*)*/;
  public static final String CONFIGURATION_FILE = /*$$(*/ "Configuration file" /*)*/;
  public static final String CONFIGURATION_FILE_NOT_EXIST = /*$$(*/
      "%s configuration file is not exist." /*)*/;
  public static final String ROW_NOT_EMPTY = /*$$(*/ "%s row must not be empty." /*)*/;
  public static final String CELL_NOT_VALID = /*$$(*/ "%s cell is not valid." /*)*/;
  public static final String IMPORT_COMPLETED_MESSAGE = /*$$(*/
      "Import completed successfully. Please check the log for more details." /*)*/;
  public static final String INVALID_DATA_FORMAT_ERROR = /*$$(*/
      "Invalid data format. Please check log for more details." /*)*/;

  /* ABC Analysis */
  public static final String ABC_CLASSES_INVALID_QTY_OR_WORTH = /*$$(*/
      "The classes total quantity and total worth must equal 100%." /*)*/;
  public static final String ABC_CLASSES_NEGATIVE_OR_NULL_QTY_OR_WORTH = /*$$(*/
      "The worth and quantity value of each class must be greater than 0." /*)*/;
  public static final String ABC_ANALYSIS_ALREADY_STARTED = /*$$(*/
      "Analysis is already on going." /*)*/;

  /* DMS Import */
  public static final String DMS_IMPORT_PROCESS_SUCCESS_MESSAGE = /*$$(*/
      "File loaded successfully" /*)*/;
  public static final String DMS_IMPORT_FILE_PROCESS_ERROR = /*$$(*/
      "Error while processing zip file" /*)*/;
  public static final String DMS_IMPORT_INVALID_ZIP_ERROR = /*$$(*/
      "Uploaded file is not a valid zip file" /*)*/;

  /** Advanced Import */
  public static final String ADVANCED_IMPORT_NO_IMPORT_FILE = /*$$(*/
      "Data file doesn't exist" /*)*/;

  public static final String ADVANCED_IMPORT_FILE_FORMAT_INVALID = /*$$(*/
      "Data file format is invalid" /*)*/;
  public static final String ADVANCED_IMPORT_ATTACHMENT_FORMAT = /*$$(*/
      "Attachments must be in zip format" /*)*/;
  public static final String ADVANCED_IMPORT_1 = /*$$(*/
      "Field(%s) doesn't exist for the object(%s)" /*)*/;
  public static final String ADVANCED_IMPORT_2 = /*$$(*/
      "Sub field(%s) doesn't exist of field(%s) for the object(%s)" /*)*/;
  public static final String ADVANCED_IMPORT_3 = /*$$(*/
      "Config exist in the file. Please check 'Config included in file'" /*)*/;
  public static final String ADVANCED_IMPORT_4 = /*$$(*/
      "Config doesn't exist in the file. Please uncheck 'Config included in file'" /*)*/;
  public static final String ADVANCED_IMPORT_5 = /*$$(*/
      "Sub field doesn't exist of field(%s) for the object(%s)" /*)*/;
  public static final String ADVANCED_IMPORT_6 = /*$$(*/
      "Please enter search call or search fields for the object(%s)" /*)*/;
  public static final String ADVANCED_IMPORT_TAB_ERR = /*$$(*/
      "File tab is not matched inside file" /*)*/;
  public static final String ADVANCED_IMPORT_NO_OBJECT = /*$$(*/
      "Object is missing for tab configuration(%s)" /*)*/;
  public static final String ADVANCED_IMPORT_NO_FIELDS = /*$$(*/
      "There is no field for tab configuration(%s)" /*)*/;
  public static final String ADVANCED_IMPORT_CHECK_LOG = /*$$(*/
      "Check log file in tabs configuration" /*)*/;
  public static final String ADVANCED_IMPORT_IMPORT_DATA = /*$$(*/
      "Data imported successfully" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_1 = /*$$(*/ "Object is not matched" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_2 = /*$$(*/ "Missing import fields" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_3 = /*$$(*/ "Missing required fields" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_4 = /*$$(*/ "Missing sub fields for" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_5 = /*$$(*/ "Fields can't be ignore" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_6 = /*$$(*/
      "Missing date format or expression" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_7 = /*$$(*/ "Invalid fields" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_8 = /*$$(*/
      "Missing data for required fields" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_9 = /*$$(*/ "Invalid type of data" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_10 = /*$$(*/
      "Action does not exists or invalid for the object(%s)" /*)*/;
  public static final String ADVANCED_IMPORT_LOG_11 = /*$$(*/
      "Invalid Search call for the object(%s)" /*)*/;
  public static final String ADVANCED_IMPORT_RESET = /*$$(*/
      "Reset imported data successfully" /*)*/;
  public static final String ADVANCED_IMPORT_NO_RESET = /*$$(*/ "No imported data to reset" /*)*/;
  public static final String SERVER_CONNECTION_ERROR = /*$$(*/
      "Unable to connect to Geonames server" /*)*/;
  public static final String DUPLICATE_ACTIVE_BANK_DETAILS = /*$$(*/
      "The same bank details are already active." /*)*/;

  // product category service
  public static final String PRODUCT_CATEGORY_PARENTS_CIRCULAR_DEPENDENCY = /*$$(*/
      "Configuration error: the product category %s is an ancestor of himself." /*)*/;
  public static final String PRODUCT_CATEGORY_CHILDREN_CIRCULAR_DEPENDENCY = /*$$(*/
      "Configuration error: the product category %s is among its descendants." /*)*/;
  /* Print template */
  public static final String PRINT_TEMPLATE_CONDITION_MUST_BE_BOOLEAN = /*$$(*/
      "The groovy condition result must be a boolean" /*)*/;
  public static final String PRINT_ERROR = /*$$(*/
      "Error in print. Please check report configuration and print settings." /*)*/;
  public static final String PRINT_TEMPLATE_ERROR_ON_LINE_WITH_SEQUENCE_AND_TITLE = /*$$(*/
      "Error in print template line with sequence %s and title %s." /*)*/;

  /* Scheduler */
  public static final String QUARTZ_SCHEDULER_ENABLED = /*$$(*/
      "The scheduler service is enabled." /*)*/;

  /* Geonames import */
  public static final String NO_DATA_FILE_FOUND = /*$$(*/ "No file found on %s." /*)*/;
  public static final String GEONAMES_DUMP_URL_NOT_SPECIFIED = /*$$(*/
      "Geoname's URL to access Geoname Dump files is missing to complete this operation. In the configuration of the App. Base, please fill in the field GeoNames Dump URL in tab Interfaces and in the panel GeoNames." /*)*/;
  public static final String GEONAMES_ZIP_URL_NOT_SPECIFIED = /*$$(*/
      "Geoname's URL to access Zip files is missing to complete this operation. In the configuration of the App. Base, please fill in the field GeoNames Zip URL in tab Interfaces and in the panel GeoNames." /*)*/;
  public static final String INVALID_GEONAMES_IMPORT_FILE = /*$$(*/
      "The geonames file type is invalid." /*)*/;
  public static final String INVALID_DATA_FILE_EXTENSION = /*$$(*/
      "Please upload csv or txt or zip files only." /*)*/;
  public static final String NO_TEXT_FILE_FOUND = /*$$(*/ "%s file not found in %s." /*)*/;

  public static final String INVALID_FIELD = /*$$(*/ "'%s' field for '%s' is invalid." /*)*/;

  /* Pricing scale */
  public static final String PRICING_1 = /*$$(*/
      "Multiple pricings found for the product/category '%s', company '%s' and model '%s', only one expected." /*)*/;

  public static final String PRICING_2 = /*$$(*/
      "There is already a pricing that has the selected pricing as previous pricing, for the formula '%s', company '%s' and model '%s'." /*)*/;

  public static final String PRICING_3 = /*$$(*/
      "You are using a product for which the '%s' pricing should be applied.</br>However, it could not be applied.</br>Please check your pricing if this does not seem normal." /*)*/;

  public static final String PREVIOUS_PERIOD_NOT_TEMP_CLOSED = /*$$(*/
      "The previous period is not closed or temporarily closed while it should be." /*)*/;

  public static final String PREVIOUS_PERIOD_NOT_CLOSED = /*$$(*/
      "The previous period is not closed." /*)*/;

  public static final String CITIES_IMPORT_FAILED = /*$$(*/
      "Error: Cities cannot be imported. Please see the attached error file for more details" /*)*/;

  public static final String META_JSON_TYPE_NO_MATCH_OBJECT_VALUE = /*$$(*/
      "Error: The type of the field %s does not match the type of the value %s" /*)*/;
  public static final String META_JSON_TYPE_NOT_MANAGED = /*$$(*/
      "Error: The type of the field %s is not managed by the adapter" /*)*/;

  public static final String COMPANY_MISSING = /*$$(*/ "Please set a company." /*)*/;

  public static final String PRICING_INVALID_DATES = /*$$(*/
      "The start date cannot be later than the end date." /*)*/;

  public static final String NO_ACTIVE_COMPANY = /*$$(*/ "Please set an active company." /*)*/;

  public static final String FILE_SOURCE_CONNECTOR_CONNECTION_TYPE_NULL = /*$$(*/
      "You must select a connection type in order to create a session" /*)*/;

  public static final String FILE_SOURCE_CONNECTOR_CONNECTION_MISSING_FIELDS = /*$$(*/
      "File source connector is missing mandatory field(s) in order to create a session." /*)*/;

  public static final String FILE_TRANSFERT_SESSION_NOT_STARTED = /*$$(*/
      "The file transfert session is not started" /*)*/;
  public static final String FILE_TRANSFERT_SESSION_NOT_CONFIGURED = /*$$(*/
      "The file transfert session is not configured" /*)*/;
  public static final String FILE_TRANSFERT_SESSION_DOWNLOAD_FAILED = /*$$(*/
      "Download failed: %s" /*)*/;
  public static final String FILE_TRANSFERT_SESSION_UPLOAD_FAILED = /*$$(*/
      "Upload failed: %s" /*)*/;

  public static final String ADVANCED_IMPORT_NOT_VALIDATED = /*$$(*/
      "Advanced import is not validated" /*)*/;

  public static final String FIELD_NOT_FOUND = /*$$(*/ "This field %s doesn't exist." /*)*/;

  public static final String ERROR_MISSING_RESEARCH_PARAMETER_CONFIGURATION = /*$$(*/
      "Missing parameter configuration for model %s." /*)*/;

  public static final String ERROR_MISSING_CANNOT_FIND_OBJECT = /*$$(*/
      "Can not find object." /*)*/;

  public static final String COMPANY_INVALID_LOCALE = /*$$(*/
      "Warning: locale %s is invalid, please enter a valid locale." /*)*/;

  public static final String COMPANY_LOCALE_MISSING = /*$$(*/
      "Please fill locale for the company %s" /*)*/;

  public static final String FAKER_METHOD_DOES_NOT_EXIST = /*$$(*/
      "The method '%s' doesn't exist in the Faker API." /*)*/;

  public static final String FAKER_CLASS_DOES_NOT_EXIST = /*$$(*/
      "The class '%s' doesn't exist in the Faker API." /*)*/;

  public static final String FAKER_METHOD_ERROR = /*$$(*/
      "An error occured while executing '%s'." /*)*/;

  public static final String JSON_FIELD_CAN_NOT_BE_ANONYMIZED = /*$$(*/
      "Json field can not be anonymized" /*)*/;

  public static final String FAKER_FIELDS_EMPTY = /*$$(*/ "The fields are empty" /*)*/;

  public static final String FAKER_METHOD_PARAMS_ERROR = /*$$(*/
      "There is a type error on parameters of the method '%s'." /*)*/;

  public static final String FAKER_CLASS_NAME_ERROR = /*$$(*/
      "Error in class name. Please check." /*)*/;

  public static final String FAKER_METHOD_NAME_ERROR = /*$$(*/
      "Error in method name. Please check." /*)*/;

  public static final String FAKER_METHOD_MISSING_PARAMS = /*$$(*/
      "Please check yours params, both fields must be filled." /*)*/;

  public static final String FAKER_METHOD_CONFIGURATION_ERROR = /*$$(*/
      "An error occurred, please check your configuration." /*)*/;

  public static final String FAKER_METHOD_PARAMETERS_CONFIGURATION_ERROR = /*$$(*/
      "Please check your parameters configuration." /*)*/;

  public static final String FAKER_METHOD_PARAMETERS_VALUE_ERROR = /*$$(*/
      "Please check your parameters value format." /*)*/;

  public static final String FAKER_METHOD_EXAMPLE_OUTPUT = /*$$(*/
      "The faker API field is valide. An example output is : %s." /*)*/;

  public static final String UNKNOWN_DURATION = /*$$(*/
      "The duration '%s' is unknown. Valid durations are in months or years." /*)*/;

  public static final String PRODUCT_MISSING_UNITS_TO_CONVERT = /*$$(*/
      "The product %s is missing stock unit or purchase unit to make the conversion of last purchase price." /*)*/;

  public static final String TEMPLATE_CONFIG_NOT_FOUND = /*$$(*/
      "The configuration to print this model has not been found" /*)*/;

  public static final String SIGNING_PDF_ERROR = /*$$(*/ "Error while signing pdf : %s" /*)*/;

  public static final String CONVERT_IMAGE_TO_PDF_ERROR = /*$$(*/
      "Error while converting image to pdf : %s" /*)*/;

  public static final String NO_RECORD_SELECTED_TO_PRINT = /*$$(*/
      "Please select records to print" /*)*/;

  public static final String PFX_CERTIFICATE_WRONG_PASSWORD = /*$$(*/
      "Error while opening the certificate, please check the password." /*)*/;

  public static final String PFX_CERTIFICATE_WRONG_FILE = /*$$(*/
      "Error with the certificate, please check if the file is a PFX certificate." /*)*/;

  public static final String PFX_CERTIFICATE_ACCESS_ERROR = /*$$(*/
      "Error while accessing certificate information." /*)*/;

  public static final String VIEW_NOT_FOUND = /*$$(*/ "No view configured for model %s." /*)*/;

  public static final String FILE_COULD_NOT_BE_GENERATED = /*$$(*/
      "The file could not be generated." /*)*/;
  public static final String SEQUENCE_GROOVY_CONFIGURATION = /*$$(*/
      "An error occurred while generating the sequence. Please check the configuration." /*)*/;

  public static final String ADDRESS_TEMPLATE_ERROR = /*$$(*/
      "An error occurred while generating the address template: '%s'" /*)*/;

  public static final String ADDRESS_FIELD_TEMPLATE_ERROR = /*$$(*/
      "An error occurred while generating the address template: '%s' - '%s'" /*)*/;

  public static final String PRICING_UNAVAILABLE_FOR_THIS_CLASS = /*$$(*/
      "Pricing process unavailable for this class : %s" /*)*/;
  public static final String REGISTRATION_CODE_EMPTY_FOR_COMPANIES = /*$$(*/
      "Registration code is required for companies" /*)*/;

  /** Import Configuration */
  public static final String IMPORT_CONFIGURATION_ERROR_MESSAGE = /*$$(*/
      "There is an error in the import configuration." /*)*/;

  public static final String IMPORT_CONFIGURATION_CLOSING_MESSAGE = /*$$(*/
      "The import configuration executed successfully." /*)*/;

  public static final String IMPORT_CONFIGURATION_WRONG_BINDING_FILE_TYPE_MESSAGE = /*$$(*/
      "The binding file should be only an XML file." /*)*/;

  public static final String IMPORT_CONFIGURATION_WRONG_DATA_FILE_TYPE_CSV_MESSAGE = /*$$(*/
      "The data file should be only a CSV or ZIP file." /*)*/;

  public static final String IMPORT_CONFIGURATION_WRONG_DATA_FILE_TYPE_XML_MESSAGE = /*$$(*/
      "The data file should be only an XML or ZIP file." /*)*/;

  public static final String MISSING_ADDRESS_FIELD = /*$$(*/ "Address Field is missing : %s" /*)*/;

  public static final String FACTORY_NO_FOUND = /*$$(*/
      "Factory not found this type of generator" /*)*/;

  public static final String MISSING_BIRT_PARAMETER = /*$$(*/ "Missing %s parameter(s)" /*)*/;

  public static final String PRINTING_TEMPLATE_SCRIPT_ERROR = /*$$(*/
      "Error when computing the printing filename, using template %s: %s" /*)*/;

  public static final String NO_QUANTITY_PROVIDED = /*$$(*/
      "This product comes in multiple quantities, so please specify a quantity that is a multiple of at least one from the list %s." /*)*/;

  public static final String QUANTITY_NOT_MULTIPLE = /*$$(*/
      "The product %s comes in multiple quantities, so the quantity provided should be a multiple of at least one from the list %s." /*)*/;

  public static final String NO_DEFAULT_ADDRESS_TEMPLATE = /*$$(*/
      "Please fill the default address template." /*)*/;

  public static final String NO_COUNTRY_FOUND = /*$$(*/ "No country found for: %s." /*)*/;
  public static final String CITY_AND_ZIP_BOTH_EMPTY = /*$$(*/
      "The city and postcode cannot be empty at the same time." /*)*/;
  public static final String NO_CITY_FOUND = /*$$(*/ "No city found" /*)*/;
  public static final String NO_ZIP_FOUND = /*$$(*/
      "No zip found in request body or in the found city" /*)*/;
  public static final String NO_ADDRESS_FOUND_WITH_INFO = /*$$(*/
      "No address found with this country, zip and street name. City name is required to create a new city." /*)*/;

  public static final String DATA_SHARING_MISSING_ELEMENTS = /*$$(*/
      "Please fill in or modify some elements to be able to register" /*)*/;

  public static final String LOCALIZATION_EMPTY = /*$$(*/ "Localization is empty" /*)*/;

  public static final String LOCALIZATION_LANGUAGE_EMPTY = /*$$(*/
      "Language is empty for the localization %s" /*)*/;

  public static final String DATA_SHARING_REFERENTIAL_LINE_JPQL_SYNTAX_IS_WRONG = /*$$(*/
      "Wrong JPQL syntax : %s" /*)*/;
  public static final String DATA_SHARING_REFERENTIAL_LINE_JPQL_SYNTAX_IS_CORRECT = /*$$(*/
      "The syntax of the script is correct." /*)*/;

  public static final String APP_BASE_NO_UNIT_DAYS = /*$$(*/
      "There is no configured unit days in the app base config" /*)*/;

  public static final String APP_BASE_NO_UNIT_HOURS = /*$$(*/
      "There is no configured unit hours in the app base config" /*)*/;

  public static final String APP_BASE_NO_UNIT_MINUTES = /*$$(*/
      "There is no configured unit minutes in the app base config" /*)*/;

  public static final String APP_BASE_NO_UNIT_DAILY_WORK_HOURS = /*$$(*/
      "There is no configured daily work hours in the app base config" /*)*/;

  public static final String API_BAD_REQUEST = /*$$(*/
      "Bad request please check api configuration information." /*)*/;

  public static final String API_WRONG_CREDENTIALS = /*$$(*/
      "Bad request please check credentials." /*)*/;

  public static final String API_WRONG_SIRET_NUMBER = /*$$(*/
      "Cannot get information with siret: %s ." /*)*/;

  public static final String API_INVALID_SIRET_NUMBER = /*$$(*/
      "Invalid SIRET number. It must contain exactly 14 digits." /*)*/;

  public static final String PARTNER_REGISTRATION_CODE_ALREADY_EXISTS = /*$$(*/
      "Registration number already exists for partner %s." /*)*/;

  public static final String APP_BASE_SIRENE_API_TOKEN_GENERATOR_URL_MISSING = /*$$(*/
      "Please fill API Sirene token generator url in app base." /*)*/;

  public static final String APP_BASE_SIRENE_API_URL_MISSING = /*$$(*/
      "Please fill API Sirene url in app base." /*)*/;

  public static final String APP_BASE_SIRENE_API_KEY_MISSING = /*$$(*/
      "Please fill API Sirene key in app base." /*)*/;

  public static final String APP_BASE_SIRENE_API_SECRET_MISSING = /*$$(*/
      "Please fill API Sirene secret in app base." /*)*/;

  public static final String APP_BASE_SIRENE_API_ACCESS_TOKEN_MISSING = /*$$(*/
      "Please fill API Sirene access token in app base." /*)*/;
}
