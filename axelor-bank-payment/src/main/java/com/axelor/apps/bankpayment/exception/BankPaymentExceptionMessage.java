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
package com.axelor.apps.bankpayment.exception;

public final class BankPaymentExceptionMessage {

  private BankPaymentExceptionMessage() {}

  /** Bank statement service */
  public static final String BANK_STATEMENT_1 = /*$$(*/
      "%s : Computed balance and Ending Balance must be equal" /*)*/;

  public static final String BANK_STATEMENT_2 = /*$$(*/
      "%s : MoveLine amount is not equals with bank statement line %s" /*)*/;
  public static final String BANK_STATEMENT_3 = /*$$(*/
      "%s : Bank statement line %s amount can't be null" /*)*/;

  /** Account config Bank Payment Service */
  public static final String ACCOUNT_CONFIG_41 = /*$$(*/
      "%s : Please, configure a default signer for the company %s" /*)*/;

  public static final String ACCOUNT_CONFIG_1 = /*$$(*/
      "%s : You must configure bank payment's information for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_5 = /*$$(*/
      "%s : Please, configure a sequence for the SEPA Credit Transfers and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_6 = /*$$(*/
      "%s : Please, configure a sequence for the SEPA Direct Debits and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_7 = /*$$(*/
      "%s : Please, configure a sequence for the International Credit Transfers and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_8 = /*$$(*/
      "%s : Please, configure a sequence for the International Direct Debits and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_9 = /*$$(*/
      "%s : Please, configure a sequence for the International Treasury Transfers and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_10 = /*$$(*/
      "%s : Please, configure a sequence for the National Treasury Transfers and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_11 = /*$$(*/
      "%s : Please, configure a sequence for the Other Bank Orders and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_12 = /*$$(*/
      "%s : Please, configure a sequence for the Bill of exchange and the company %s" /*)*/;

  public static final String ACCOUNT_CONFIG_EXTERNAL_BANK_TO_BANK_ACCOUNT = /*$$(*/
      "%s : Please, configure an account for the bank order for the external bank to bank transfer for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_INTERNAL_BANK_TO_BANK_ACCOUNT = /*$$(*/
      "%s : Please, configure an account for the bank order for the internal bank to bank transfer for the company %s" /*)*/;

  public static final String ACCOUNT_CONFIG_MISSING_ICS_NUMBER = /*$$(*/
      "%s : Please configure an ICS number for the company %s." /*)*/;

  /** BankOrder service */
  public static final String BANK_ORDER_DATE = /*$$(*/ "Bank Order date can't be in the past" /*)*/;

  public static final String BANK_ORDER_DATE_MISSING = /*$$(*/ "Please fill bank order date" /*)*/;
  public static final String BANK_ORDER_TYPE_MISSING = /*$$(*/ "Please fill bank order type" /*)*/;
  public static final String BANK_ORDER_PARTNER_TYPE_MISSING = /*$$(*/
      "Please fill partner type for the bank order" /*)*/;
  public static final String BANK_ORDER_COMPANY_MISSING = /*$$(*/
      "Please fill the sender company" /*)*/;
  public static final String BANK_ORDER_BANK_DETAILS_MISSING = /*$$(*/
      "Please fill the bank details" /*)*/;
  public static final String BANK_ORDER_CURRENCY_MISSING = /*$$(*/
      "Please fill currency for the bank order" /*)*/;
  public static final String BANK_ORDER_AMOUNT_NEGATIVE = /*$$(*/
      "Amount value of the bank order is not valid" /*)*/;
  public static final String BANK_ORDER_PAYMENT_MODE_MISSING = /*$$(*/
      "Please select a payment mode" /*)*/;
  public static final String BANK_ORDER_SIGNATORY_MISSING = /*$$(*/
      "Please select a signatory" /*)*/;
  public static final String BANK_ORDER_WRONG_SENDER_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the sender record of the bank order %s" /*)*/;
  public static final String BANK_ORDER_WRONG_MAIN_DETAIL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the detail record of the bank order line %s" /*)*/;
  public static final String BANK_ORDER_WRONG_ENDORSED_DETAIL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the endorsed detail record of the bank order line %s" /*)*/;
  public static final String BANK_ORDER_WRONG_ADDITIONAL_DETAIL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the additional detail record of the bank order line %s" /*)*/;
  public static final String BANK_ORDER_WRONG_BENEFICIARY_BANK_DETAIL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the beneficiary bank detail record of the bank order line %s" /*)*/;
  public static final String BANK_ORDER_WRONG_FURTHER_INFORMATION_DETAIL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the further information detail record of the bank order line %s" /*)*/;
  public static final String BANK_ORDER_WRONG_TOTAL_RECORD = /*$$(*/
      "Anomaly has been detected during file generation for the total record of the bank order %s" /*)*/;
  public static final String BANK_ORDER_ISSUE_DURING_FILE_GENERATION = /*$$(*/
      "Anomaly has been detected during file generation for bank order %s" /*)*/;
  public static final String BANK_ORDER_COMPANY_NO_SEQUENCE = /*$$(*/
      "The company %s does not have bank order sequence" /*)*/;
  public static final String BANK_ORDER_BANK_DETAILS_EMPTY_IBAN = /*$$(*/
      "The Iban is mandatory for the partner %s, bank order %s" /*)*/;
  public static final String BANK_ORDER_BANK_DETAILS_NOT_ACTIVE = /*$$(*/
      "The sender bank details is inactive." /*)*/;
  public static final String BANK_ORDER_BANK_DETAILS_TYPE_NOT_COMPATIBLE = /*$$(*/
      "The bank details type is not compatible with the accepted types in file format." /*)*/;
  public static final String BANK_ORDER_BANK_DETAILS_CURRENCY_NOT_COMPATIBLE = /*$$(*/
      "The sender bank details currency is not compatible with the currency in bank order." /*)*/;
  public static final String BANK_ORDER_BANK_DETAILS_MISSING_CURRENCY = /*$$(*/
      "Please fill the sender bank details currency." /*)*/;
  public static final String BANK_ORDER_NOT_PROPERLY_SIGNED = /*$$(*/
      "The bank order is not properly signed. Please correct it and sign it again." /*)*/;
  public static final String BANK_ORDER_CANNOT_REMOVE = /*$$(*/
      "Bank orders can only be deleted at draft or canceled status." /*)*/;
  public static final String BANK_ORDER_NO_SENDER_CURRENCY = /*$$(*/
      "Please set a currency in the sender bank details : %s." /*)*/;

  public static final String BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_BANK_ADDRESS = /*$$(*/
      "Please fill the bank address in the receiver bank details." /*)*/;
  public static final String BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_PARTNER_ADDRESS = /*$$(*/
      "Please fill the address in %s's partner details." /*)*/;
  public static final String BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_PARTNER_ZIP = /*$$(*/
      "Please fill the zip in %s's partner details." /*)*/;
  public static final String BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_PARTNER_CITY = /*$$(*/
      "Please fill the city in %s's partner details." /*)*/;
  public static final String BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_BANK = /*$$(*/
      "Please fill the bank in the receiver bank details." /*)*/;

  /** BankOrder lines */
  public static final String BANK_ORDER_LINES_MISSING = /*$$(*/
      "You can't validate this bank order. you need to fill at least one bank order line" /*)*/;

  public static final String BANK_ORDER_LINE_COMPANY_MISSING = /*$$(*/
      "Please select a company for the bank order lines inserted" /*)*/;
  public static final String BANK_ORDER_LINE_PARTNER_MISSING = /*$$(*/
      "Please select a partner for the bank order lines inserted" /*)*/;
  public static final String BANK_ORDER_LINE_AMOUNT_NEGATIVE = /*$$(*/
      "Amount value of a bank order line is not valid" /*)*/;
  public static final String BANK_ORDER_LINE_TOTAL_AMOUNT_INVALID = /*$$(*/
      "Total amount of bank order lines must be equal to the bank order amount" /*)*/;
  public static final String BANK_ORDER_LINE_BANK_DETAILS_MISSING = /*$$(*/
      "Please fill the receiver bank details" /*)*/;
  public static final String BANK_ORDER_LINE_BANK_DETAILS_FORBIDDEN = /*$$(*/
      "You cannot use this bank account because he is not authorized by the ebics partner." /*)*/;
  public static final String BANK_ORDER_LINE_BANK_DETAILS_NOT_ACTIVE = /*$$(*/
      "The receiver bank details for the line %s is inactive." /*)*/;
  public static final String BANK_ORDER_LINE_BANK_DETAILS_TYPE_NOT_COMPATIBLE = /*$$(*/
      "The receiver bank details type is not compatible with the accepted types in file format." /*)*/;
  public static final String BANK_ORDER_LINE_BANK_DETAILS_CURRENCY_NOT_COMPATIBLE = /*$$(*/
      "The receiver bank details currency is not compatible with the currency in bank order." /*)*/;
  public static final String BANK_ORDER_LINE_NO_RECEIVER_ADDRESS = /*$$(*/
      "No address has been defined in the receiver %s" /*)*/;
  public static final String BANK_ORDER_LINE_ORIGIN_NO_DMS_FILE = /*$$(*/
      "There is no file linked to this origin." /*)*/;

  /** BankOrder merge */
  public static final String BANK_ORDER_MERGE_AT_LEAST_TWO_BANK_ORDERS = /*$$(*/
      "Please select at least two bank orders" /*)*/;

  public static final String BANK_ORDER_MERGE_STATUS = /*$$(*/
      "Please select draft or awaiting signature bank orders only" /*)*/;
  public static final String BANK_ORDER_MERGE_SAME_STATUS = /*$$(*/
      "Please select some bank orders that have the same status" /*)*/;
  public static final String BANK_ORDER_MERGE_SAME_ORDER_TYPE_SELECT = /*$$(*/
      "Please select some bank orders that have the same status" /*)*/;
  public static final String BANK_ORDER_MERGE_SAME_PAYMENT_MODE = /*$$(*/
      "Please select some bank orders that have the same payment mode" /*)*/;
  public static final String BANK_ORDER_MERGE_SAME_PARTNER_TYPE_SELECT = /*$$(*/
      "Please select some bank orders that have the same partner type" /*)*/;
  public static final String BANK_ORDER_MERGE_SAME_SENDER_COMPANY = /*$$(*/
      "Please select some bank orders that have the same sender company" /*)*/;
  public static final String BANK_ORDER_MERGE_SAME_SENDER_BANK_DETAILS = /*$$(*/
      "Please select some bank orders that have the same sender bank details" /*)*/;
  public static final String BANK_ORDER_MERGE_SAME_CURRENCY = /*$$(*/
      "Please select some bank orders that have the same currency" /*)*/;
  public static final String BANK_ORDER_MERGE_NO_BANK_ORDERS = /*$$(*/ "No bank orders found" /*)*/;

  /** BankOrder file */
  public static final String BANK_ORDER_FILE_NO_SENDER_ADDRESS = /*$$(*/
      "No address has been defined in the sender company %s" /*)*/;

  public static final String BANK_ORDER_FILE_NO_FOLDER_PATH = /*$$(*/
      "No folder path has been defined in the payment mode %s" /*)*/;
  public static final String BANK_ORDER_FILE_UNKNOWN_FORMAT = /*$$(*/
      "Unknown format for file generation" /*)*/;
  public static final String BANK_ORDER_FILE_UNKNOWN_SEPA_TYPE = /*$$(*/
      "Unknown SEPA type for file generation" /*)*/;

  /** Ebics */
  public static final String EBICS_WRONG_PASSWORD = /*$$(*/
      "Incorrect password, please try again" /*)*/;

  public static final String EBICS_MISSING_PASSWORD = /*$$(*/ "Please insert a password" /*)*/;
  public static final String EBICS_MISSING_NAME = /*$$(*/ "Please select a user name" /*)*/;
  public static final String EBICS_TEST_MODE_NOT_ENABLED = /*$$(*/
      "Test mode is not enabled or test file is missing" /*)*/;
  public static final String EBICS_MISSING_CERTIFICATES = /*$$(*/
      "Please add certificates to print" /*)*/;
  public static final String EBICS_INVALID_BANK_URL = /*$$(*/
      "Invalid bank url. It must be start with http:// or https://" /*)*/;
  public static final String EBICS_MISSING_USER_TRANSPORT = /*$$(*/
      "Please insert a EBICS user for transport in the EBICS partner" /*)*/;
  public static final String EBICS_NO_SERVICE_CONFIGURED = /*$$(*/
      "No service configured on EBICS partner %s for file format %s" /*)*/;
  public static final String EBICS_PARTNER_BANK_DETAILS_WARNING = /*$$(*/
      "At least one bank details you have entered is missing currency. Here is the list of invalid bank details : %s" /*)*/;
  public static final String EBICS_MISSING_SIGNATORY_EBICS_USER = /* $$( */
      "Signatory EBICS user is missing." /* ) */;

  /** Batch bank statement */
  public static final String BATCH_BANK_STATEMENT_RETRIEVED_BANK_STATEMENT_COUNT = /*$$(*/
      "Number of retrieved bank statements: %d." /*)*/;

  /** BankStatement import */
  public static final String BANK_STATEMENT_FILE_UNKNOWN_FORMAT = /*$$(*/
      "Unknown format for file import process" /*)*/;

  public static final String BANK_STATEMENT_MISSING_FILE = /*$$(*/
      "Missing bank statement file" /*)*/;
  public static final String BANK_STATEMENT_MISSING_FILE_FORMAT = /*$$(*/
      "Missing bank statement file format" /*)*/;
  public static final String BANK_STATEMENT_EBICS_PARTNER = /*$$(*/
      "Error with EBICS partner %s: %s" /*)*/;
  public static final String BANK_STATEMENT_ALREADY_IMPORTED = /*$$(*/
      "Bank statement already imported. Aborted." /*)*/;
  public static final String BANK_STATEMENT_NOT_MATCHING = /*$$(*/
      "Current bank statement's initial balance does not match previous bank statement's final balance" /*)*/;
  public static final String BANK_STATEMENT_INCOHERENT_BALANCE = /*$$(*/
      "The balances of the bank statement are incoherent and are not following. The bankStatement file can't be imported." /*)*/;
  public static final String BANK_STATEMENT_NO_INITIAL_LINE_ON_IMPORT = /*$$(*/
      "Current bank statement doesn't contain an initial line" /*)*/;

  /*
   * Batch direct debit
   */
  public static final String BATCH_DIRECT_DEBIT_MISSING_COMPANY_BANK_DETAILS = /*$$(*/
      "Company bank details is missing in batch." /*)*/;
  public static final String BATCH_DIRECT_DEBIT_NO_PROCESSED_PAYMENT_SCHEDULE_LINES = /*$$(*/
      "No processed payment schedule lines" /*)*/;
  public static final String BATCH_DIRECT_DEBIT_UNKNOWN_DATA_TYPE = /*$$(*/
      "Unknown direct debit data type" /*)*/;
  public static final String DIRECT_DEBIT_MISSING_PARTNER_ACTIVE_UMR = /*$$(*/
      "Please add an Active UMR to the partner." /*)*/;

  /** Generate bank order from invoices */
  public static final String INVOICE_BANK_ORDER_ALREADY_EXIST = /*$$(*/
      "A bank order %s already exist for the invoice %s." /*)*/;

  public static final String
      MOVE_LINE_ARCHIVE_NOT_OK_BECAUSE_OF_BANK_RECONCILIATION_AMOUNT = /*$$(*/
          "This move line %s can not be archived because its bank reconciliation amount is superior to 0." /*)*/;

  public static final String INVOICE_PAYMENT_MODE_MISSING = /*$$(*/
      "Payment mode is missing on the invoice %s" /*)*/;

  /** Bank reconciliation */
  public static final String BANK_RECONCILIATION_MISSING_JOURNAL = /*$$(*/
      "Some entries from the reconciliation have an empty moveLine and an account filled. The journal is required to generate automatically the Move/Move Lines for those entries." /*)*/;

  /** Bank reconciliation */
  public static final String BANK_RECONCILIATION_MISSING_CASH_ACCOUNT = /*$$(*/
      "Some entries from the reconciliation have an empty moveLine and an account filled. The cash account is required to generate automatically the counterpart Move Lines for those entries." /*)*/;

  public static final String BANK_RECONCILIATION_UNRECONCILE_NO_SELECT = /*$$(*/
      "Please select a reconciliation line" /*)*/;
  public static final String BANK_RECONCILIATION_INCOMPLETE_LINE = /*$$(*/
      "To validate the reconciliation, each line must be marked with one or more move line, either existing or configured (Account, Third party). A move line will be generated automatically on the account and journal associated with the reconciliation session." /*)*/;

  public static final String
      BANK_RECONCILIATION_SELECT_MOVE_LINE_AND_BANK_RECONCILIATION_LINE = /*$$(*/
          "Please select one bank reconciliation line and one move line" /*)*/;
  public static final String BANK_RECONCILIATION_SELECT_BANK_RECONCILIATION_LINE = /*$$(*/
      "Please select one bank reconciliation line" /*)*/;
  public static final String BANK_RECONCILIATION_SELECT_MOVE_LINE = /*$$(*/
      "Please select one move line" /*)*/;
  public static final String BANK_RECONCILIATION_ALREADY_OPEN = /*$$(*/
      "Can't load while another reconciliation is open" /*)*/;
  public static final String BANK_RECONCILIATION_BANK_STATEMENT_NO_BANK_DETAIL = /*$$(*/
      "The selected bank statement doesn't contain, at the lines level, any information allowing to identify which bank details it concerns. Please verify the format of the data source or the configuration of the bank details in the software and please make sure both are matching." /*)*/;
  public static final String BANK_RECONCILIATION_CANNOT_DELETE_VALIDATED = /*$$(*/
      "Selected bank reconciliation is validated and can not be deleted" /*)*/;
  public static final String BANK_RECONCILIATION_CANNOT_DELETE_UNDER_CORRECTION = /*$$(*/
      "Selected bank reconciliation is under correction and can not be deleted" /*)*/;

  public static final String BANK_RECONCILIATION_NO_DISTRIBUTION_GENERATED_MOVE_LINE = /*$$(*/
      "The analytic distribution is required in the move lines for account %s but no template has been set in the account configuration" /*)*/;

  /** Bank Statement Query */
  public static final String BANK_STATEMENT_QUERY_SEQUENCE_USED = /*$$(*/
      "Sequence is already used" /*)*/;

  public static final String BATCH_BOE_SEND_BILLING_PARTNER_ADRESS_MISSING = /*$$(*/
      "Email adress is missing for partner %s" /*)*/;

  public static final String PAYMENT_SESSION_GENERATED_BANK_ORDER = /*$$(*/
      "The bank order %s has been generated successfully." /*)*/;

  public static final String
      BANK_STATEMENT_MOVE_LINE_QUERY_FORMULA_NOT_EVALUATED_TO_MOVE_LINE = /*$$(*/
          "Move line's query formula has not been evaluated to a Move line" /*)*/;
  public static final String BANK_STATEMENT_PARTNER_QUERY_FORMULA_NOT_EVALUATED_TO_PARTNER = /*$$(*/
      "Partner's query formula has not been evaluated to a Partner" /*)*/;

  public static final String BANK_STATEMENT_RULE_CASH_ACCOUNT_MISSING = /*$$(*/
      "Please select a cash account in the bank statement rule %s" /*)*/;

  public static final String BANK_STATEMENT_RULE_COUNTERPART_ACCOUNT_MISSING = /*$$(*/
      "Please select a counterpart account in the bank statement rule %s" /*)*/;

  public static final String STATEMENT_REMOVE_NOT_OK_NB = /*$$(*/
      "%d bank statement couldn't be deleted, please check the logs." /*)*/;

  public static final String STATEMENT_REMOVE_OK = /*$$(*/
      "Bank statement(s) has been removed successfully" /*)*/;

  public static final String NO_STATEMENT_TO_REMOVE = /*$$(*/ "Please select statements" /*)*/;

  /** Move Reverse */
  public static final String MOVE_LINKED_TO_VALIDATED_BANK_RECONCILIATION = /*$$(*/
      "The move %s can't be reversed because it is linked to a bank reconciliation with status validated" /*)*/;

  public static final String MOVES_LINKED_TO_VALIDATED_BANK_RECONCILIATION = /*$$(*/
      "The moves %s couldn't be reversed because these are linked to a bank reconciliation with status validated" /*)*/;

  public static final String VALIDATION_BANK_ORDER_MOVE_INV_PAYMENT_FAIL = /*$$(*/
      "Failed to create a move for the invoice payment." /*)*/;

  public static final String MOVE_LINE_CHECK_BANK_RECONCILED_AMOUNT = /*$$(*/
      "Bank reconcile amount must be inferior or equal to credit or debit." /*)*/;

  public static final String BATCH_BILL_OF_EXCHANGE_BANK_DETAILS_IS_MISSING_ON_INVOICE = /*$$(*/
      "Bank details is missing on invoice %s." /*)*/;

  public static final String BATCH_BILL_OF_EXCHANGE_BANK_DETAILS_IS_INACTIVE_ON_INVOICE = /*$$(*/
      "The bank details %s attached to the invoice/ invoice term %s and to the partner %s is inactive. Only invoices with active bank details can be processed." /*)*/;

  public static final String BANK_ACCOUNT_DIFFERENT_THAN_CASH_ACCOUNT = /*$$(*/
      "Your bank detail's bank account for bank details %s (%s) is different from the cash account of the account config for %s's bank statement rule (%s). Please fix it before auto accounting the bank statement." /*)*/;
}
