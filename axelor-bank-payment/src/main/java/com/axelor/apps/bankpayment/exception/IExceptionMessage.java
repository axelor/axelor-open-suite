/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 *
 * @author dubaux
 */
public interface IExceptionMessage {

  /** Bank statement service */
  static final String BANK_STATEMENT_1 = /*$$(*/
      "%s : Computed balance and Ending Balance must be equal" /*)*/;

  static final String BANK_STATEMENT_2 = /*$$(*/
      "%s : MoveLine amount is not equals with bank statement line %s" /*)*/;
  static final String BANK_STATEMENT_3 = /*$$(*/
      "%s : Bank statement line %s amount can't be null" /*)*/;

  /** Account config Bank Payment Service */
  static final String ACCOUNT_CONFIG_41 = /*$$(*/
      "%s : Please, configure a default signer for the company %s" /*)*/;

  static final String ACCOUNT_CONFIG_1 = /*$$(*/
      "%s : You must configure bank payment's information for the company %s" /*)*/;
  static final String ACCOUNT_CONFIG_SEQUENCE_5 = /*$$(*/
      "%s : Please, configure a sequence for the SEPA Credit Transfers and the company %s" /*)*/;
  static final String ACCOUNT_CONFIG_SEQUENCE_6 = /*$$(*/
      "%s : Please, configure a sequence for the SEPA Direct Debits and the company %s" /*)*/;
  static final String ACCOUNT_CONFIG_SEQUENCE_7 = /*$$(*/
      "%s : Please, configure a sequence for the International Credit Transfers and the company %s" /*)*/;
  static final String ACCOUNT_CONFIG_SEQUENCE_8 = /*$$(*/
      "%s : Please, configure a sequence for the International Direct Debits and the company %s" /*)*/;
  static final String ACCOUNT_CONFIG_SEQUENCE_9 = /*$$(*/
      "%s : Please, configure a sequence for the International Treasury Transfers and the company %s" /*)*/;
  static final String ACCOUNT_CONFIG_SEQUENCE_10 = /*$$(*/
      "%s : Please, configure a sequence for the National Treasury Transfers and the company %s" /*)*/;
  static final String ACCOUNT_CONFIG_SEQUENCE_11 = /*$$(*/
      "%s : Please, configure a sequence for the Other Bank Orders and the company %s" /*)*/;

  static final String ACCOUNT_CONFIG_EXTERNAL_BANK_TO_BANK_ACCOUNT = /*$$(*/
      "%s : Please, configure an account for the bank order for the external bank to bank transfer for the company %s" /*)*/;
  static final String ACCOUNT_CONFIG_INTERNAL_BANK_TO_BANK_ACCOUNT = /*$$(*/
      "%s : Please, configure an account for the bank order for the internal bank to bank transfer for the company %s" /*)*/;

  static final String ACCOUNT_CONFIG_MISSING_ICS_NUMBER = /*$$(*/
      "%s : Please configure an ICS number for the company %s." /*)*/;

  /** BankOrder service */
  static final String BANK_ORDER_DATE = /*$$(*/ "Bank Order date can't be in the past" /*)*/;

  static final String BANK_ORDER_DATE_MISSING = /*$$(*/ "Please fill bank order date" /*)*/;
  static final String BANK_ORDER_TYPE_MISSING = /*$$(*/ "Please fill bank order type" /*)*/;
  static final String BANK_ORDER_PARTNER_TYPE_MISSING = /*$$(*/
      "Please fill partner type for the bank order" /*)*/;
  static final String BANK_ORDER_COMPANY_MISSING = /*$$(*/ "Please fill the sender company" /*)*/;
  static final String BANK_ORDER_BANK_DETAILS_MISSING = /*$$(*/
      "Please fill the bank details" /*)*/;
  static final String BANK_ORDER_CURRENCY_MISSING = /*$$(*/
      "Please fill currency for the bank order" /*)*/;
  static final String BANK_ORDER_AMOUNT_NEGATIVE = /*$$(*/
      "Amount value of the bank order is not valid" /*)*/;
  static final String BANK_ORDER_PAYMENT_MODE_MISSING = /*$$(*/
      "Please select a payment mode" /*)*/;
  static final String BANK_ORDER_SIGNATORY_MISSING = /*$$(*/ "Please select a signatory" /*)*/;
  static final String BANK_ORDER_WRONG_SENDER_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the sender record of the bank order %s" /*)*/;
  static final String BANK_ORDER_WRONG_MAIN_DETAIL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the detail record of the bank order line %s" /*)*/;
  static final String BANK_ORDER_WRONG_BENEFICIARY_BANK_DETAIL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the beneficiary bank detail record of the bank order line %s" /*)*/;
  static final String BANK_ORDER_WRONG_FURTHER_INFORMATION_DETAIL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the further information detail record of the bank order line %s" /*)*/;
  static final String BANK_ORDER_WRONG_TOTAL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the total record of the bank order %s" /*)*/;
  static final String BANK_ORDER_ISSUE_DURING_FILE_GENERATION = /*$$(*/
      "Anomaly has been detected during file generation for bank order %s" /*)*/;
  static final String BANK_ORDER_COMPANY_NO_SEQUENCE = /*$$(*/
      "The company %s does not have bank order sequence" /*)*/;
  static final String BANK_ORDER_BANK_DETAILS_EMPTY_IBAN = /*$$(*/
      "The Iban is mandatory for the partner %s, bank order %s" /*)*/;
  static final String BANK_ORDER_BANK_DETAILS_NOT_ACTIVE = /*$$(*/
      "The bank details is inactive." /*)*/;
  static final String BANK_ORDER_BANK_DETAILS_TYPE_NOT_COMPATIBLE = /*$$(*/
      "The bank details type is not compatible with the accepted types in file format." /*)*/;
  static final String BANK_ORDER_BANK_DETAILS_CURRENCY_NOT_COMPATIBLE = /*$$(*/
      "The sender bank details currency is not compatible with the currency in bank order." /*)*/;
  static final String BANK_ORDER_BANK_DETAILS_MISSING_CURRENCY = /*$$(*/
      "Please fill the sender bank details currency." /*)*/;
  static final String BANK_ORDER_NOT_PROPERLY_SIGNED = /*$$(*/
      "The bank order is not properly signed. Please correct it and sign it again." /*)*/;
  static final String BANK_ORDER_CANNOT_REMOVE = /*$$(*/
      "Bank orders can only be deleted at draft or canceled status." /*)*/;
  static final String BANK_ORDER_NO_SENDER_CURRENCY = /*$$(*/
      "Please set a currency in the sender bank details : %s." /*)*/;

  String BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_BANK_ADDRESS = /*$$(*/
      "Please fill the bank address in the receiver bank details." /*)*/;
  String BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_BANK = /*$$(*/
      "Please fill the bank in the receiver bank details." /*)*/;

  /** BankOrder lines */
  static final String BANK_ORDER_LINES_MISSING = /*$$(*/
      "You can't validate this bank order. you need to fill at least one bank order line" /*)*/;

  static final String BANK_ORDER_LINE_COMPANY_MISSING = /*$$(*/
      "Please select a company for the bank order lines inserted" /*)*/;
  static final String BANK_ORDER_LINE_PARTNER_MISSING = /*$$(*/
      "Please select a partner for the bank order lines inserted" /*)*/;
  static final String BANK_ORDER_LINE_AMOUNT_NEGATIVE = /*$$(*/
      "Amount value of a bank order line is not valid" /*)*/;
  static final String BANK_ORDER_LINE_TOTAL_AMOUNT_INVALID = /*$$(*/
      "Total amount of bank order lines must be equal to the bank order amount" /*)*/;
  static final String BANK_ORDER_LINE_BANK_DETAILS_MISSING = /*$$(*/
      "Please fill the receiver bank details" /*)*/;
  static final String BANK_ORDER_LINE_BANK_DETAILS_FORBIDDEN = /*$$(*/
      "You cannot use this bank account because he is not authorized by the ebics partner." /*)*/;
  static final String BANK_ORDER_LINE_BANK_DETAILS_NOT_ACTIVE = /*$$(*/
      "The receiver bank details is inactive." /*)*/;
  static final String BANK_ORDER_LINE_BANK_DETAILS_TYPE_NOT_COMPATIBLE = /*$$(*/
      "The receiver bank details type is not compatible with the accepted types in file format." /*)*/;
  static final String BANK_ORDER_LINE_BANK_DETAILS_CURRENCY_NOT_COMPATIBLE = /*$$(*/
      "The receiver bank details currency is not compatible with the currency in bank order." /*)*/;
  static final String BANK_ORDER_LINE_NO_RECEIVER_ADDRESS = /*$$(*/
      "No address has been defined in the receiver %s" /*)*/;

  /** BankOrder merge */
  static final String BANK_ORDER_MERGE_AT_LEAST_TWO_BANK_ORDERS = /*$$(*/
      "Please select at least two bank orders" /*)*/;

  static final String BANK_ORDER_MERGE_STATUS = /*$$(*/
      "Please select draft or awaiting signature bank orders only" /*)*/;
  static final String BANK_ORDER_MERGE_SAME_STATUS = /*$$(*/
      "Please select some bank orders that have the same status" /*)*/;
  static final String BANK_ORDER_MERGE_SAME_ORDER_TYPE_SELECT = /*$$(*/
      "Please select some bank orders that have the same status" /*)*/;
  static final String BANK_ORDER_MERGE_SAME_PAYMENT_MODE = /*$$(*/
      "Please select some bank orders that have the same payment mode" /*)*/;
  static final String BANK_ORDER_MERGE_SAME_PARTNER_TYPE_SELECT = /*$$(*/
      "Please select some bank orders that have the same partner type" /*)*/;
  static final String BANK_ORDER_MERGE_SAME_SENDER_COMPANY = /*$$(*/
      "Please select some bank orders that have the same sender company" /*)*/;
  static final String BANK_ORDER_MERGE_SAME_SENDER_BANK_DETAILS = /*$$(*/
      "Please select some bank orders that have the same sender bank details" /*)*/;
  static final String BANK_ORDER_MERGE_SAME_CURRENCY = /*$$(*/
      "Please select some bank orders that have the same currency" /*)*/;
  static final String BANK_ORDER_MERGE_NO_BANK_ORDERS = /*$$(*/ "No bank orders found" /*)*/;

  /** BankOrder file */
  static final String BANK_ORDER_FILE_NO_SENDER_ADDRESS = /*$$(*/
      "No address has been defined in the sender company %s" /*)*/;

  static final String BANK_ORDER_FILE_NO_FOLDER_PATH = /*$$(*/
      "No folder path has been defined in the payment mode %s" /*)*/;
  static final String BANK_ORDER_FILE_UNKNOWN_FORMAT = /*$$(*/
      "Unknown format for file generation" /*)*/;
  static final String BANK_ORDER_FILE_UNKNOWN_SEPA_TYPE = /*$$(*/
      "Unknown SEPA type for file generation" /*)*/;

  /** Ebics */
  static final String EBICS_WRONG_PASSWORD = /*$$(*/ "Incorrect password, please try again" /*)*/;

  static final String EBICS_MISSING_PASSWORD = /*$$(*/ "Please insert a password" /*)*/;
  static final String EBICS_MISSING_NAME = /*$$(*/ "Please select a user name" /*)*/;
  static final String EBICS_TEST_MODE_NOT_ENABLED = /*$$(*/
      "Test mode is not enabled or test file is missing" /*)*/;
  static final String EBICS_MISSING_CERTIFICATES = /*$$(*/ "Please add certificates to print" /*)*/;
  static final String EBICS_INVALID_BANK_URL = /*$$(*/
      "Invalid bank url. It must be start with http:// or https://" /*)*/;
  static final String EBICS_MISSING_USER_TRANSPORT = /*$$(*/
      "Please insert a EBICS user for transport in the EBICS partner" /*)*/;
  static final String EBICS_NO_SERVICE_CONFIGURED = /*$$(*/
      "No service configured on EBICS partner %s for file format %s" /*)*/;
  static final String EBICS_PARTNER_BANK_DETAILS_WARNING = /*$$(*/
      "At least one bank details you have entered is missing currency. Here is the list of invalid bank details : %s" /*)*/;
  static final String EBICS_MISSING_SIGNATORY_EBICS_USER = /* $$( */
      "Signatory EBICS user is missing." /* ) */;

  /** Batch bank statement */
  String BATCH_BANK_STATEMENT_RETRIEVED_BANK_STATEMENT_COUNT = /*$$(*/
      "Number of retrieved bank statements: %d." /*)*/;

  /** BankStatement import */
  static final String BANK_STATEMENT_FILE_UNKNOWN_FORMAT = /*$$(*/
      "Unknown format for file import process" /*)*/;

  static final String BANK_STATEMENT_MISSING_FILE = /*$$(*/ "Missing bank statement file" /*)*/;
  static final String BANK_STATEMENT_MISSING_FILE_FORMAT = /*$$(*/
      "Missing bank statement file format" /*)*/;
  static final String BANK_STATEMENT_EBICS_PARTNER = /*$$(*/
      "Error with EBICS partner %s: %s" /*)*/;
  static final String BANK_STATEMENT_ALREADY_IMPORTED = /*$$(*/
      "Bank statement already imported. Aborted." /*)*/;
  static final String BANK_STATEMENT_NOT_MATCHING = /*$$(*/
      "Current bank statement's initial balance does not match previous bank statement's final balance" /*)*/;
  static final String BANK_STATEMENT_INCOHERENT_BALANCE = /*$$(*/
      "The balances of the bank statement are incoherent and are not following. The bankStatement file can't be imported." /*)*/;
  static final String BANK_STATEMENT_NO_INITIAL_LINE_ON_IMPORT = /*$$(*/
      "Current bank statement doesn't contain an initial line" /*)*/;

  /*
   * Batch direct debit
   */
  static final String BATCH_DIRECT_DEBIT_MISSING_COMPANY_BANK_DETAILS = /*$$(*/
      "Company bank details is missing in batch." /*)*/;
  static final String BATCH_DIRECT_DEBIT_NO_PROCESSED_PAYMENT_SCHEDULE_LINES = /*$$(*/
      "No processed payment schedule lines" /*)*/;
  static final String BATCH_DIRECT_DEBIT_UNKNOWN_DATA_TYPE = /*$$(*/
      "Unknown direct debit data type" /*)*/;
  static final String DIRECT_DEBIT_MISSING_PARTNER_ACTIVE_UMR = /*$$(*/
      "Please add an Active UMR to the partner." /*)*/;

  /** Generate bank order from invoices */
  static final String INVOICE_BANK_ORDER_ALREADY_EXIST = /*$$(*/
      "A bank order %s already exist for the invoice %s." /*)*/;

  static final String MOVE_LINE_ARCHIVE_NOT_OK_BECAUSE_OF_BANK_RECONCILIATION_AMOUNT = /*$$(*/
      "This move line %s can not be archived because its bank reconciliation amount is superior to 0." /*)*/;

  static final String INVOICE_PAYMENT_MODE_MISSING = /*$$(*/
      "Payment mode is missing on the invoice %s" /*)*/;

  /** Bank reconciliation */
  static final String BANK_RECONCILIATION_MISSING_JOURNAL = /*$$(*/
      "Some entries from the reconciliation have an empty moveLine and an account filled. The journal is required to generate automatically the Move/Move Lines for those entries." /*)*/;

  /** Bank reconciliation */
  static final String BANK_RECONCILIATION_MISSING_CASH_ACCOUNT = /*$$(*/
      "Some entries from the reconciliation have an empty moveLine and an account filled. The cash account is required to generate automatically the counterpart Move Lines for those entries." /*)*/;

  static final String BANK_RECONCILIATION_UNRECONCILE_NO_SELECT = /*$$(*/
      "Please select a reconciliation line" /*)*/;
  static final String BANK_RECONCILIATION_INCOMPLETE_LINE = /*$$(*/
      "To validate the reconciliation, each line must be marked with one or more move line, either existing or configured (Account, Third party). A move line will be generated automatically on the account and journal associated with the reconciliation session." /*)*/;

  static final String BANK_RECONCILIATION_SELECT_MOVE_LINE_AND_BANK_RECONCILIATION_LINE = /*$$(*/
      "Please select one bank reconciliation line and one move line" /*)*/;
  static final String BANK_RECONCILIATION_SELECT_BANK_RECONCILIATION_LINE = /*$$(*/
      "Please select one bank reconciliation line" /*)*/;
  static final String BANK_RECONCILIATION_SELECT_MOVE_LINE = /*$$(*/
      "Please select one move line" /*)*/;
  static final String BANK_RECONCILIATION_ALREADY_OPEN = /*$$(*/
      "Can't load while another bank reconciliation is in progress" /*)*/;
  static final String BANK_RECONCILIATION_BANK_STATEMENT_NO_BANK_DETAIL = /*$$(*/
      "The selected bank statement doesn't contain, at the lines level, any information allowing to identify which bank details it concerns. Please verify the format of the data source or the configuration of the bank details in the software and please make sure both are matching." /*)*/;

  /** Bank Statement Query */
  static final String BANK_STATEMENT_QUERY_SEQUENCE_USED = /*$$(*/ "Sequence is already used" /*)*/;

  static final String BANK_STATEMENT_RULE_CASH_ACCOUNT_MISSING = /*$$(*/
      "Please select a cash account in the bank statement rule %s" /*)*/;

  static final String BANK_STATEMENT_RULE_COUNTERPART_ACCOUNT_MISSING = /*$$(*/
      "Please select a counterpart account in the bank statement rule %s" /*)*/;
}
