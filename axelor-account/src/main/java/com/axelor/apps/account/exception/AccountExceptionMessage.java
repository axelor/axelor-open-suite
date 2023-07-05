/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.exception;

public final class AccountExceptionMessage {

  private AccountExceptionMessage() {}

  public static final String INVOICE_DUPLICATED_WITHIN_SAME_YEAR =
      /*$$(*/ "An invoice with the same number (%s) already exists for this supplier %s and the year %s." /*)*/;
  public static final String IMMO_FIXED_ASSET_CAN_NOT_SIMULATE =
      /*$$(*/ "The line can not be simulated because there is no journal or it does not authorize simulated move" /*)*/;
  public static final String IMMO_FIXED_ASSET_DEPRECIATIONS_NOT_ACCOUNTED_BEFORE_DISPOSAL_DATE =
      /*$$(*/ "The fixed asset can't be disposed at the moment as there are still depreciations that remains to be accounted before input disposal date. Please proceed to the realization of those depreciations before proceeding to the disposal of the asset on that date." /*)*/;
  public static final String IMMO_FIXED_ASSET_DISPOSAL_QTY_GREATER_ORIGINAL =
      /*$$(*/ "Disposal quantity can not be greater than the fixed asset quantity (%s)" /*)*/;
  public static final String IMMO_FIXED_ASSET_DISPOSAL_QTY_EQUAL_ORIGINAL_MAX =
      /*$$(*/ "Disposal quantity can not be equal to the fixed asset max quantity (%s)" /*)*/;
  public static final String IMMO_FIXED_ASSET_DISPOSAL_QTY_EQUAL_0 =
      /*$$(*/ "Disposal quantity can not be equal to 0" /*)*/;
  public static final String IMMO_FIXED_ASSET_LINE_PREVIOUS_NOT_REALIZED =
      /*$$(*/ "Line can't be realized because previous line is still planned" /*)*/;
  public static final String IMMO_FIXED_ASSET_GENERATE_SALE_MOVE_CATEGORY_ACCOUNTS_MISSING =
      /*$$(*/ "Fixed asset: sale move could not be generated because fixed category is missing one of these accounts : %s" /*)*/;
  public static final String IMMO_FIXED_ASSET_GENERATE_DISPOSAL_MOVE_CATEGORY_ACCOUNTS_MISSING =
      /*$$(*/ "Fixed asset: Disposal move could not be generated because the following account setting : '%s' is missing on the associated fixed asset category." /*)*/;
  public static final String IMMO_FIXED_ASSET_GENERATE_MOVE_CATEGORY_ACCOUNTS_MISSING =
      /*$$(*/ "Fixed asset: the depreciation move(s) cannot be generated because of the following missing account(s) setting(s) on the fixed asset category : %s." /*)*/;
  public static final String IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING =
      /*$$(*/ "Fixed asset: fixed asset category is missing one of these accounts : %s" /*)*/;
  public static final String IMMO_FIXED_ASSET_MISSING_DEROGATORY_LINE =
      /*$$(*/ "Fixed asset is missing a derogatory line at status planned" /*)*/;
  public static final String IMMO_FIXED_ASSET_CESSION_BEFORE_FIRST_SERVICE_DATE =
      /*$$(*/ "Disposal date can not be before the first service date of the fixed asset" /*)*/;
  public static final String IMMO_FIXED_ASSET_VALIDATE_GROSS_VALUE_0 =
      /*$$(*/ "The gross value of a fixed asset must be greater than zero. The fixed asset %s can't be validated." /*)*/;
  public static final String IMMO_FIXED_ASSET_FAILOVER_CONTROL_ONLY_LINEAR =
      /*$$(*/ "The reimport process of fixed asser is only available for fixed asset depreciated with the linear method, with the Economic and fiscal methode being equal" /*)*/;
  public static final String
      IMMO_FIXED_ASSET_FAILOVER_CONTROL_PAST_DEPRECIATION_GREATER_THAN_GROSS_VALUE =
          /*$$(*/ "The input past depreciation amount cannot be greater than the gross value of the fixed asset." /*)*/;
  public static final String IMMO_FIXED_ASSET_FAILOVER_CONTROL_NON_CONSISTENT_VALUES =
      /*$$(*/ "When on failover alreadyDepreciatedAmount and NbrOfPastDepreciation must be greater than 0." /*)*/;
  public static final String FIXED_ASSET_CAN_NOT_BE_REMOVE =
      /*$$(*/ "Only fixed assets at draft status can be deleted." /*)*/;
  public static final String INVOICE_LINE_TAX_LINE = /*$$(*/ "A tax line is missing" /*)*/;
  /** Bank statement service */
  public static final String BANK_STATEMENT_1 = /*$$(*/
      "%s : Computed balance and Ending Balance must be equal" /*)*/;

  public static final String BANK_STATEMENT_2 = /*$$(*/
      "%s : MoveLine amount is not equals with bank statement line %s" /*)*/;
  public static final String BANK_STATEMENT_3 = /*$$(*/
      "%s : Bank statement line %s amount can't be null" /*)*/;

  /** Move service */
  public static final String NO_MOVES_SELECTED = /*$$(*/
      "Please select 'Draft' or 'Simulated' moves" /*)*/;

  public static final String NO_NEW_MOVES_SELECTED = /*$$(*/
      "Only the records in status Draft and on a journal allowing simulated entries are shifted to Simulated status" /*)*/;

  public static final String MOVE_ACCOUNTING_NOT_OK = /*$$(*/
      "Error or anomaly detected making it impossible to proceed for the following move accounting operation : %s . Please check the logs." /*)*/;
  public static final String MOVE_ACCOUNTING_OK = /*$$(*/
      "The selected moves have been successfully, if activated set to daybook, or else accounted." /*)*/;
  public static final String MOVE_SIMULATION_OK = /*$$(*/ "Moves simulated successfully" /*)*/;
  public static final String MOVE_ARCHIVE_NOT_OK = /*$$(*/ "You can't archive this move %s" /*)*/;
  public static final String MOVE_REMOVE_NOT_OK = /*$$(*/ "You can't remove this move %s" /*)*/;
  public static final String MOVE_REMOVED_OK = /*$$(*/ "Move has been removed successfully" /*)*/;
  public static final String MOVE_ARCHIVE_OK = /*$$(*/
      "Move(s) has been archived successfully" /*)*/;
  public static final String NO_MOVE_TO_REMOVE_OR_ARCHIVE = /*$$(*/
      "Please select 'Draft' or 'Accounted' or 'Canceled' moves" /*)*/;
  public static final String MOVE_ARCHIVE_OR_REMOVE_OK = /*$$(*/
      "Selected move(s) successfully removed" /*)*/;
  public static final String MOVE_ARCHIVE_OR_REMOVE_NOT_OK = /*$$(*/
      "Error in move deleting or archiving, please check the log" /*)*/;
  public static final String MOVE_ARCHIVE_NOT_OK_BECAUSE_OF_LINK_WITH = /*$$(*/
      "This move %s can not be archived because it is linked to another piece named %s." /*)*/;
  public static final String MOVE_LINE_ARCHIVE_NOT_OK_BECAUSE_OF_LINK_WITH = /*$$(*/
      "This move line %s can not be archived because it is linked to another piece named %s." /*)*/;
  public static final String MOVE_LINE_RECONCILE_LINE_CANNOT_BE_REMOVED = /*$$(*/
      "The move lines %s are reconciled and should not have been removed." /*)*/;
  public static final String MOVE_LINE_GENERATION_FIXED_ASSET_MISSING_DESCRIPTION = /*$$(*/
      "The move line %s is missing description in order to create fixed asset" /*)*/;
  public static final String MOVE_ARCHIVE_OR_REMOVE_NOT_OK_NB = /*$$(*/
      "%d moves couldn't be deleted or archived, please check the logs." /*)*/;

  /** Account management repostiroy */
  public static final String ACCOUNT_MANAGEMENT_ALREADY_EXISTS = /*$$(*/
      "A payment mode already exists with the same information." /*)*/;

  /** Account management service */
  public static final String ACCOUNT_MANAGEMENT_1_ACCOUNT = /*$$(*/
      "Accounting configuration is missing for Product: %s (company: %s)" /*)*/;

  public static final String ACCOUNT_MANAGEMENT_2 = /*$$(*/
      "Account of financial discount is missing for company: %s (tax: %s)" /*)*/;

  /** AccountingSituationService * */
  public static final String ACCOUNTING_SITUATION_1 = /*$$(*/
      "You have to enter a prefix for automatic customer account creation in accounting application parameters of company %s" /*)*/;

  public static final String ACCOUNTING_SITUATION_2 = /*$$(*/
      "You have to select a sequence for automatic customer account creation in accounting application parameters of company %s" /*)*/;
  public static final String ACCOUNTING_SITUATION_3 = /*$$(*/
      "Invalid automatic partner account creation mode, this is a bug that should be reported." /*)*/;
  public static final String ACCOUNTING_SITUATION_4 = /*$$(*/
      "You have to enter a prefix for automatic supplier account creation in accounting application parameters of company %s" /*)*/;
  public static final String ACCOUNTING_SITUATION_5 = /*$$(*/
      "You have to select a sequence for automatic supplier account creation in accounting application parameters of company %s" /*)*/;
  public static final String ACCOUNTING_SITUATION_6 = /*$$(*/
      "You have to enter a prefix for automatic employee account creation in accounting application parameters of company %s" /*)*/;
  public static final String ACCOUNTING_SITUATION_7 = /*$$(*/
      "You have to select a sequence for automatic employee account creation in accounting application parameters of company %s" /*)*/;

  /** Mail service */
  public static final String MAIL_1 = /*$$(*/
      "%s : Please define an email template for cash register (company: %s)" /*)*/;

  /** Account clearance service and controller */
  public static final String ACCOUNT_CLEARANCE_1 = /*$$(*/
      "%s : You must configure account information for the company %s" /*)*/;

  public static final String ACCOUNT_CLEARANCE_2 = /*$$(*/
      "%s : You must configure a financial account for then company %s" /*)*/;
  public static final String ACCOUNT_CLEARANCE_3 = /*$$(*/
      "%s : You must configure a tax standard rate for the company %s" /*)*/;
  public static final String ACCOUNT_CLEARANCE_4 = /*$$(*/
      "%s : You must configure account clearance for the company %s" /*)*/;
  public static final String ACCOUNT_CLEARANCE_5 = /*$$(*/
      "%s : You must configure a clearance sequence of the overpayments for the company %s" /*)*/;
  public static final String ACCOUNT_CLEARANCE_6 = /*$$(*/
      "%s : You must configure an overpayment account clearance journal for the company %s" /*)*/;
  public static final String ACCOUNT_CLEARANCE_7 = /*$$(*/ "Move lines generated" /*)*/;

  /** Account customer service */
  public static final String ACCOUNT_CUSTOMER_1 = /*$$(*/
      "%s : A customer account is missing for the company %s" /*)*/;

  public static final String ACCOUNT_CUSTOMER_2 = /*$$(*/
      "%s : A supplier account is missing for the company %s" /*)*/;

  /** Check rejection service */
  public static final String CHECK_REJECTION_1 = /*$$(*/
      "%s : You must configure a cheque rejection sequence for the company %s" /*)*/;

  /** Irrecoverable service and controller */
  public static final String IRRECOVERABLE_1 = /*$$(*/ "Timetable line %s" /*)*/;

  public static final String IRRECOVERABLE_2 = /*$$(*/
      "%s : Error generated on invoice creation shift to irrecoverable %s" /*)*/;
  public static final String IRRECOVERABLE_3 = /*$$(*/
      "%s : The invoice %s has no accounting document whose the remainder to be paid is positive" /*)*/;
  public static final String IRRECOVERABLE_4 = /*$$(*/
      "%s : You must configure shit to irrecoverable sequence for the company %s" /*)*/;
  public static final String IRRECOVERABLE_5 = /*$$(*/ "Treatment finished" /*)*/;
  public static final String IRRECOVERABLE_6 = /*$$(*/ "Anomalies generated" /*)*/;
  public static final String IRRECOVERABLE_7 = /*$$(*/ "You must select a printing type" /*)*/;

  /** Journal service */
  public static final String JOURNAL_1 = /*$$(*/ "Invoice type missing on invoice %s" /*)*/;

  /** AnalyticJournal service */
  public static final String NOT_UNIQUE_NAME_ANALYTIC_JOURNAL =
      /*$$(*/ "The code defined here is already used by another record for the specified %s. Code must be unique by company. Please modify it accordingly." /*)*/;
  /** Move line export service */
  public static final String MOVE_LINE_EXPORT_1 = /*$$(*/
      "%s : Error : You must configure a sale interface sequence for the company %s" /*)*/;

  public static final String MOVE_LINE_EXPORT_2 = /*$$(*/
      "%s : Error : You must configure a credit note interface sequence for the company %s" /*)*/;
  public static final String MOVE_LINE_EXPORT_3 = /*$$(*/
      "%s : Error : You must configure a treasury interface sequence for the company %s" /*)*/;
  public static final String MOVE_LINE_EXPORT_4 = /*$$(*/
      "%s : Error : You must configure a purchase interface sequence for the company %s" /*)*/;
  public static final String MOVE_LINE_EXPORT_YEAR_OR_PERIOD_OR_DATE_IS_NULL = /*$$(*/
      "Year or period or date is null, you must set a period." /*)*/;

  /** Accounting report service and controller */
  public static final String ACCOUNTING_REPORT_1 = /*$$(*/
      "%s : Error : You must configure an account reporting sequence for the company %s" /*)*/;

  public static final String ACCOUNTING_REPORT_2 = /*$$(*/
      "%s : Error : You must configure an account export sequence for the company %s" /*)*/;
  public static final String ACCOUNTING_REPORT_3 = /*$$(*/ "Lines recovered" /*)*/;
  public static final String ACCOUNTING_REPORT_4 = /*$$(*/ "You must select an export type" /*)*/;
  public static final String ACCOUNTING_REPORT_6 = /*$$(*/ "Moves exported" /*)*/;
  public static final String ACCOUNTING_REPORT_7 = /*$$(*/
      "%s : Error : You must configure a custom account reporting sequence for the company %s" /*)*/;
  public static final String ACCOUNTING_REPORT_8 = /*$$(*/ "Accounting export" /*)*/;
  public static final String ACCOUNTING_REPORT_9 = /*$$(*/
      "Error : Missing accounting report type for %s" /*)*/;
  public static final String ACCOUNTING_REPORT_UNKNOWN_ACCOUNTING_REPORT_TYPE = /*$$(*/
      "Unknown accounting report type: %d" /*)*/;
  public static final String ACCOUNTING_REPORT_NO_REPORT_TYPE = /*$$(*/
      "No report type selected" /*)*/;
  public static final String ACCOUNTING_REPORT_ANALYTIC_REPORT = /*$$(*/
      "%s : Error : You must configure an analytic report sequence for the company %s" /*)*/;
  public static final String ACCOUNTING_REPORT_REPORT_TYPE_NOT_FOUND = /*$$(*/
      "Report type not found" /*)*/;
  public static final String ACCOUNTING_REPORT_ANOMALIES = /*$$(*/ "Anomalies generated" /*)*/;

  public static final String ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER = /*$$(*/
      "Company partner is missing" /*)*/;

  public static final String ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS = /*$$(*/
      "Company partner main address is missing" /*)*/;

  public static final String ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS_L7 = /*$$(*/
      "Country is missing in company partner main address" /*)*/;

  public static final String ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS_L7_A2CODE = /*$$(*/
      "Country alpha2code is missing in company partner main address" /*)*/;

  public static final String ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS_CITY = /*$$(*/
      "City is missing in company partner main address" /*)*/;

  public static final String ACCOUNTING_REPORT_DAS2_ACTIVE_NORM = /*$$(*/
      "DAS2 active norm is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_CONTACT_MISSING = /*$$(*/
      "DAS2 contact partner is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_CONTACT_FIRST_NAME_MISSING = /*$$(*/
      "DAS2 contact partner : first name is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_CONTACT_TITLE_MISSING = /*$$(*/
      "DAS2 contact partner : title is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_CONTACT_EMAIL_MISSING = /*$$(*/
      "DAS2 contact partner : email is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_CONTACT_PHONE_MISSING = /*$$(*/
      "DAS2 contact partner : phone is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_CONTACT_WRONG_TITLE = /*$$(*/
      "DAS2 contact partner : title must be of type M. or MS." /*)*/;
  public static final String
      ACCOUNTING_REPORT_DAS2_DECLARANT_COMPANY_MISSING_REGISTRATION_CODE = /*$$(*/
          "DAS2 declarant company %s : Registration code is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_DECLARANT_COMPANY_MISSING_NAF = /*$$(*/
      "DAS2 declarant company %s : Activity code is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_DECLARANT_COMPANY_MISSING_ADDRESS = /*$$(*/
      "DAS2 declarant company %s : Address is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_MOVE_LINE_PARTNER_MISSING = /*$$(*/
      "Partner is missing on the move %s" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_TITLE_MISSING = /*$$(*/
      "DAS2 declared partner %s %s : title is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_WRONG_TITLE = /*$$(*/
      "DAS2 declared partner %s %s : title must be of type M. or MS." /*)*/;
  public static final String
      ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_REGISTRATION_CODE = /*$$(*/
          "DAS2 declared partner %s %s : Registration code is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_ADDRESS = /*$$(*/
      "DAS2 declared partner %s %s : address is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_ADDRESS_CITY = /*$$(*/
      "DAS2 declared partner %s %s : address city is missing" /*)*/;
  public static final String
      ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_ADDRESS_CITY_ZIP = /*$$(*/
          "DAS2 declared partner %s %s : address city zip is missing" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_INCONSISTENT_TITLE = /*$$(*/
      "DAS2 declared partner %s %s : a foreign declared partner is necessarily an individual" /*)*/;
  public static final String ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_FIRST_NAME_MISSING = /*$$(*/
      "DAS2 declared partner %s %s : first name is missing" /*)*/;

  /** Move line service */
  public static final String MOVE_LINE_1 = /*$$(*/ "Partner is missing on the invoice %s" /*)*/;

  public static final String MOVE_LINE_2 = /*$$(*/
      "Partner account missing on the invoice %s" /*)*/;
  public static final String MOVE_LINE_4 = /*$$(*/
      "Account missing on configuration for line : %s (company : %s)" /*)*/;
  public static final String MOVE_LINE_5 = /*$$(*/
      "Analytic account %s associated to sales account for the product %s is not configured : (company : %s)" /*)*/;
  public static final String MOVE_LINE_6 = /*$$(*/
      "Account missing on the tax line : %s (company : %s)" /*)*/;
  public static final String ANALYTIC_DISTRIBUTION_MISSING = /*$$(*/
      "Analytic distribution is missing on configuration for line : %s (company : %s)" /*)*/;
  public static final String MOVE_LINE_7 = /*$$(*/
      "The accounting move line on the account %s can't have an amount equals to zero" /*)*/;
  public static final String MOVE_LINE_MISSING_DATE = /*$$(*/ "Missing date on move line" /*)*/;
  public static final String MOVE_LINE_8 = /*$$(*/
      "Account missing on the tax line %s nor on fiscal position %s (company : %s)" /*)*/;

  public static final String MOVE_LINE_CONTROL_ACCOUNTING_ACCOUNT_FAIL = /*$$(*/
      "Designated account %s in move line %s is not allowed on the journal %s. Please modify journal settings or designated account to proceed." /*)*/;
  /** Move service */
  public static final String MOVE_1 = /*$$(*/ "Invoice type missing on invoice %s" /*)*/;

  public static final String MOVE_2 = /*$$(*/ "You must select a journal for the move %s" /*)*/;
  public static final String MOVE_3 = /*$$(*/ "You must select a company for the move %s" /*)*/;
  public static final String MOVE_4 = /*$$(*/ "You must select a period for the move %s" /*)*/;
  public static final String MOVE_5 = /*$$(*/
      "Journal %s does not have any account move sequence configured" /*)*/;
  public static final String MOVE_6 = /*$$(*/ "Move account sens %s can't be determined" /*)*/;
  public static final String MOVE_7 = /*$$(*/
      "Account move %s has a total debit different than total credit : %s <> %s" /*)*/;
  public static final String MOVE_8 = /*$$(*/ "The move %s cannot be empty" /*)*/;
  public static final String MOVE_9 = /*$$(*/
      "Tax is mandatory for the account %s (%s) on the move line %s" /*)*/;
  public static final String MOVE_10 = /*$$(*/
      "Analytic distribution template is mandatory for the account %s on the move line %s." /*)*/;
  public static final String MOVE_11 = /*$$(*/
      "An analytic distribution is set in move line %s but the account used do not allow analytic distribution" /*)*/;
  public static final String MOVE_ACCOUNTING_FISCAL_PERIOD_CLOSED = /*$$(*/
      "Accounting move can not be accounted because its fiscal period is closed." /*)*/;
  public static final String MOVE_12 = /*$$(*/
      "The currency is missing on the account move %s" /*)*/;
  public static final String MOVE_VALIDATION_FISCAL_PERIOD_CLOSED = /*$$(*/
      "Accounting move can not be validated because its fiscal period is closed." /*)*/;
  public static final String MOVE_PARTNER_IS_NOT_COMPATIBLE_WITH_SELECTED_JOURNAL = /*$$(*/
      "The partner which was already selected is not compatible with the selected journal. Please reselect a compatible partner or modify the journal settings accordingly." /*)*/;

  /** Payment schedule export service */
  public static final String PAYMENT_SCHEDULE_1 = /*$$(*/
      "%s : You must configure a RIB for payment timetable %s" /*)*/;

  public static final String PAYMENT_SCHEDULE_2 = /*$$(*/
      "%s : You must configure a RIB for the partner %s" /*)*/;
  public static final String PAYMENT_SCHEDULE_3 = /*$$(*/
      "%s : Error : You must configure a direct debit date for the %s batch configuration" /*)*/;
  public static final String PAYMENT_SCHEDULE_4 = /*$$(*/
      "%s : You must configure a direct debit reject sequence\n for the company %s for the journal %s" /*)*/;
  public static final String PAYMENT_SCHEDULE_5 = /*$$(*/
      "You must configure a timetable sequence for the company %s" /*)*/;
  public static final String PAYMENT_SCHEDULE_6 = /*$$(*/
      "%s : Error : You must, at first, create timetable lines for the timetable %s" /*)*/;
  public static final String PAYMENT_SCHEDULE_LINE_AMOUNT_MISMATCH = /*$$(*/
      "The sum of line amounts (%s) must match the amount of the payment schedule (%s)." /*)*/;

  /** Reconcile service */
  public static final String RECONCILE_1 = /*$$(*/
      "%s : Reconciliation : You must fill concerned moves lines." /*)*/;

  public static final String RECONCILE_2 = /*$$(*/
      "%s : Reconciliation : Move line accounts are not compatible." /*)*/;
  public static final String RECONCILE_3 = /*$$(*/
      "(Debit %s account %s amount %s - Credit %s account %s amount %s)" /*)*/;
  public static final String RECONCILE_4 = /*$$(*/
      "%s : Reconciliation %s: Reconciliated amount must be different than zero. (Debit %s account %s - Credit %s account %s)" /*)*/;
  public static final String RECONCILE_5 = /*$$(*/
      "%s : Reconciliation %s: Reconciliated amount (%s) must be lower or equal to remaining amount to reconciliate from moves lines." /*)*/;
  public static final String RECONCILE_6 = /*$$(*/
      "%s : Error : You must configure a reconciliation sequence for the company %s" /*)*/;
  public static final String RECONCILE_7 = /*$$(*/
      "Reconciliation : Selected moves lines must concern the same company. Reconcile : %s company \n Debit move line : %s company \n Credit move line : %s company" /*)*/;
  public static final String RECONCILE_CAN_NOT_BE_REMOVE = /*$$(*/
      "The reconcile %s cannot be removed, please select draft reconcile(s)" /*)*/;

  /** Reimbursement service and controller */
  public static final String REIMBURSEMENT_1 = /*$$(*/
      "%s : You must configure a reimbursement sequence for the company %s" /*)*/;

  public static final String REIMBURSEMENT_2 = /*$$(*/
      "Export reimbursement folder (SEPA format) has not been configured for the company %s." /*)*/;
  public static final String REIMBURSEMENT_3 = /*$$(*/
      "No reimbursement found for the ref %s and the company %s." /*)*/;
  public static final String REIMBURSEMENT_4 = /*$$(*/ "You must configure a RIB." /*)*/;

  /** Year service */
  public static final String YEAR_1 = /*$$(*/
      "%s : You must configure a company for the fiscal year %s" /*)*/;

  public static final String YEAR_2 = /*$$(*/
      "All previous fiscal years must be closed before closing %s." /*)*/;

  /** Batch Account customer */
  public static final String BATCH_ACCOUNT_1 = /*$$(*/ "Accounting situation %s" /*)*/;

  public static final String BATCH_ACCOUNT_2 = /*$$(*/
      "Contact's account balances determination's reporting :" /*)*/;
  public static final String BATCH_ACCOUNT_3 = /*$$(*/ "* %s Account(s) situation(s) treated" /*)*/;
  public static final String BATCH_ACCOUNT_4 = /*$$(*/
      "Account balances of %s accounting situation has not been updated, you must run the contact account batch update." /*)*/;
  public static final String BATCH_ACCOUNT_5 = /*$$(*/
      "Account balances from all accounts situations (%s) has been updated." /*)*/;

  /** Batch doubtful customer */
  public static final String BATCH_DOUBTFUL_1 = /*$$(*/
      "Doubtful account's determination's reporting" /*)*/;

  public static final String BATCH_DOUBTFUL_2 = /*$$(*/ "* %s Invoice(s) treated" /*)*/;

  /** Batch move line export */
  public static final String BATCH_MOVELINE_EXPORT_1 = /*$$(*/
      "%s : Error : You must configure a company for the batch configurator %s" /*)*/;

  public static final String BATCH_MOVELINE_EXPORT_2 = /*$$(*/
      "%s : Error : You must configure a due date for the batch configurator %s" /*)*/;
  public static final String BATCH_MOVELINE_EXPORT_3 = /*$$(*/
      "%s : Error : You must configure an export type for the batch configurator %s" /*)*/;
  public static final String BATCH_MOVELINE_EXPORT_4 = /*$$(*/
      "Moves export batch's reporting :" /*)*/;
  public static final String BATCH_MOVELINE_EXPORT_5 = /*$$(*/ "Moves Lines (Moves) exported" /*)*/;

  /** Batch payment schedule import/export */
  public static final String BATCH_PAYMENT_SCHEDULE_1 = /*$$(*/
      "Unknowned data type for the treatment %s" /*)*/;

  public static final String BATCH_PAYMENT_SCHEDULE_2 = /*$$(*/
      "Direct debit's export batch %s" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_3 = /*$$(*/ "Due date's direct debit %s" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_4 = /*$$(*/
      "Export reporting to invoices direct debits :" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_5 = /*$$(*/
      "Invoice(s) direct debit(s) treated" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_6 = /*$$(*/
      "Export reporting to monthly direct debits :" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_7 = /*$$(*/
      "Monthly direct debit(s) treated" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_8 = /*$$(*/
      "%s : No timetable nor invoice found for the direct debit number : %s" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_9 = /*$$(*/ "Reject %s" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_10 = /*$$(*/
      "Timetable's reject move's creation %s" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_11 = /*$$(*/
      "Invoice's reject move's creation %s" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_12 = /*$$(*/
      "Reporting to direct debit reject's import :" /*)*/;
  public static final String BATCH_PAYMENT_SCHEDULE_13 = /*$$(*/ "Direct debit(s) rejected" /*)*/;

  /** Batch reimbursement export/import */
  public static final String BATCH_REIMBURSEMENT_1 = /*$$(*/
      "Bug(Anomaly) generated during SEPA export - Batch %s" /*)*/;

  public static final String BATCH_REIMBURSEMENT_2 = /*$$(*/
      "Reporting to reimbursement creation :" /*)*/;
  public static final String BATCH_REIMBURSEMENT_3 = /*$$(*/ "Reimbursement(s) created" /*)*/;
  public static final String BATCH_REIMBURSEMENT_4 = /*$$(*/
      "Reporting to reimbursement's export :" /*)*/;
  public static final String BATCH_REIMBURSEMENT_5 = /*$$(*/ "Reimbursement(s) treated" /*)*/;
  public static final String BATCH_REIMBURSEMENT_6 = /*$$(*/
      "Reimbursement's import's batch %s" /*)*/;
  public static final String BATCH_REIMBURSEMENT_7 = /*$$(*/ "Reimbursement reject %s" /*)*/;
  public static final String BATCH_REIMBURSEMENT_8 = /*$$(*/
      "Reporting to reimbursement reject's import :" /*)*/;
  public static final String BATCH_REIMBURSEMENT_9 = /*$$(*/ "Reimbursement(s) rejected" /*)*/;
  public static final String BATCH_REIMBURSEMENT_10 = /*$$(*/ "Total Amount" /*)*/;

  /** Batch debt recovery */
  public static final String BATCH_DEBT_RECOVERY_1 = /*$$(*/ "Debt recovery's reporting :" /*)*/;

  public static final String BATCH_DEBT_RECOVERY_2 = /*$$(*/ "Partner(s) treated" /*)*/;

  /** Batch credit transfer */
  public static final String BATCH_CREDIT_TRANSFER_REPORT_TITLE = /*$$(*/
      "Report for credit transfer batch:" /*)*/;

  public static final String BATCH_CREDIT_TRANSFER_INVOICE_DONE_SINGULAR = /*$$(*/
      "%d invoice treated successfully," /*)*/;
  public static final String BATCH_CREDIT_TRANSFER_INVOICE_DONE_PLURAL = /*$$(*/
      "%d invoices treated successfully," /*)*/;
  public static final String BATCH_CREDIT_TRANSFER_REIMBURSEMENT_DONE_SINGULAR = /*$$(*/
      "%d reimbursement created successfully," /*)*/;
  public static final String BATCH_CREDIT_TRANSFER_REIMBURSEMENT_DONE_PLURAL = /*$$(*/
      "%d reimbursements created successfully," /*)*/;
  public static final String BATCH_CREDIT_TRANSFER_ANOMALY_SINGULAR = /*$$(*/ "%d anomaly." /*)*/;
  public static final String BATCH_CREDIT_TRANSFER_ANOMALY_PLURAL = /*$$(*/ "%d anomalies." /*)*/;

  public static final String BATCH_CREDIT_TRANSFER_BANK_DETAILS_MISSING = /*$$(*/
      "%s : Please, fill bank details in batch %s" /*)*/;

  public static final String BATCH_CREDIT_TRANSFER_PAYMENT_MODE_MISSING = /*$$(*/
      "%s : Please, fill payment mode in batch %s" /*)*/;

  /** Batch strategy */
  public static final String BATCH_STRATEGY_1 = /*$$(*/
      "%s : You must configure a RIB for batch's configurator %s" /*)*/;

  /** Batch realize fixed asset lines */
  public static final String BATCH_REALIZED_FIXED_ASSET_LINE = /*$$(*/
      "Realized fixed asset lines" /*)*/;

  public static final String BATCH_PROCESSED_FIXED_ASSET = /*$$(*/ "Processed fixed assets" /*)*/;

  public static final String BATCH_PROCESSED_FIXED_ASSET_LINE_ECONOMIC = /*$$(*/
      "Realized fixed asset economic lines" /*)*/;

  public static final String BATCH_PROCESSED_FIXED_ASSET_LINE_FISCAL = /*$$(*/
      "Realized fixed asset fiscal lines" /*)*/;

  public static final String BATCH_PROCESSED_FIXED_ASSET_LINE_IFRS = /*$$(*/
      "Realized fixed assets ifrs lines" /*)*/;

  public static final String BATCH_PROCESSED_FIXED_ASSET_DEROGATORY_LINE = /*$$(*/
      "Realized fixed assets derogatory lines" /*)*/;

  /** Batch close / open the year account */
  public static final String BATCH_CLOSE_OPEN_ANNUAL_ACCOUNT_REPORT_TITLE = /*$$(*/
      "Report for close/open annual accounts batch:" /*)*/;

  public static final String BATCH_CLOSE_OPEN_ANNUAL_ACCOUNT_DONE_SINGULAR = /*$$(*/
      "%d account treated successfully," /*)*/;
  public static final String BATCH_CLOSE_OPEN_ANNUAL_ACCOUNT_DONE_PLURAL = /*$$(*/
      "%d accounts treated successfully," /*)*/;

  /** Cfonb export service */
  public static final String CFONB_EXPORT_1 = /*$$(*/
      "You must configure a RIB for the reimbursement" /*)*/;

  public static final String CFONB_EXPORT_2 = /*$$(*/
      "%s : Error detected during CFONB file's writing : %s" /*)*/;
  public static final String CFONB_EXPORT_3 = /*$$(*/
      "%s : You must configure a Sort Code for the RIB %s of third-payer %s" /*)*/;
  public static final String CFONB_EXPORT_4 = /*$$(*/
      "%s : You must configure a number's account for the RIB %s of third-payer %s" /*)*/;
  public static final String CFONB_EXPORT_5 = /*$$(*/
      "%s : You must configure a Bank Code for the RIB %s of third-payer %s" /*)*/;
  public static final String CFONB_EXPORT_6 = /*$$(*/
      "%s : You must configure a Bank Address for the RIB %s of third-payer %s" /*)*/;

  /** Cfonb import service */
  public static final String CFONB_IMPORT_1 = /*$$(*/
      "%s : You must configure a reject/return reason's code's list relating to Card cashing, Direct debit and TIP in general configuration" /*)*/;

  public static final String CFONB_IMPORT_2 = /*$$(*/
      "%s : A header record is missing in the file %s" /*)*/;
  public static final String CFONB_IMPORT_3 = /*$$(*/
      "%s : One or several detail records are missing in the file %s" /*)*/;
  public static final String CFONB_IMPORT_4 = /*$$(*/
      "%s : A record is missing in the file %s" /*)*/;
  public static final String CFONB_IMPORT_5 = /*$$(*/
      "%s : The total amount for the following record isn't correct (file %s) :\n %s" /*)*/;
  public static final String CFONB_IMPORT_6 = /*$$(*/
      "%s : No payment mode found for the code %s and the company %s" /*)*/;

  /** Cfonb tool service */
  public static final String CFONB_TOOL_NB_OF_CHAR_PER_LINE = /*$$(*/
      "The record is not %s characters" /*)*/;

  public static final String CFONB_TOOL_EMPTY_ZONE = /*$$(*/ "Zone %s is empty" /*)*/;
  public static final String CFONB_TOOL_DIGITAL_ZONE_NOT_CORRECT = /*$$(*/
      "Zone %s (%s) must be of the numeric type" /*)*/;
  public static final String CFONB_TOOL_1 = /*$$(*/
      "%s : Anomaly detected (value isn't numeric : %s) for sender" /*)*/;
  public static final String CFONB_TOOL_2 = /*$$(*/
      "%s : Anomaly detected (value isn't numeric : %s) for the receiver" /*)*/;
  public static final String CFONB_TOOL_3 = /*$$(*/
      "%s : Anomaly detected (value isn't numeric : %s) for the total" /*)*/;
  public static final String CFONB_TOOL_4 = /*$$(*/
      "%s : Anomaly detected (the record doesn't have %s characters : %s) for the record %s, company %s" /*)*/;

  public static final String COMPANY_CURRENCY = /*$$(*/
      "%s : Please, configure a currency for the company %s" /*)*/;

  public static final String ACCOUNT_CONFIG_1 = /*$$(*/
      "%s : You must configure account's information for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_2 = /*$$(*/
      "%s : You must configure a CFONB format reimbursement's export's folder for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_3 = /*$$(*/
      "%s : You must configure a CFONB format direct debit's export's folder for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_4 = /*$$(*/
      "%s : You must configure a TIP and cheque TIP payment's import path for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_5 = /*$$(*/
      "%s : You must configure a TIP and cheque TIP temporary import path for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_6 = /*$$(*/
      "%s : You must configure a TIP and cheque TIP payment rejects path for the import file for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_7 = /*$$(*/
      "%s : You must configure a TIP and cheque TIP temporary path for the payment reject's file for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_8 = /*$$(*/
      "%s : You must configure a path for the reject's file for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_9 = /*$$(*/
      "%s : You must configure a path for the temporary reject's file for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_10 = /*$$(*/
      "%s : You must configure a path for the reimbursements rejects import's file for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_11 = /*$$(*/
      "%s : You must configure a path for the reimbursement rejects import's temporary file for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_12 = /*$$(*/
      "%s : You must configure a rejects journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_13 = /*$$(*/
      "%s : You must configure an irrevocable journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_14 = /*$$(*/
      "%s : You must configure a Supplier purchase journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_15 = /*$$(*/
      "%s : You must configure a Supplier credit note journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_16 = /*$$(*/
      "%s : You must configure a Sales journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_17 = /*$$(*/
      "%s : You must configure a Customer credit note journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_18 = /*$$(*/
      "%s : You must configure a Misc. Operation journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_19 = /*$$(*/
      "%s : You must configure a Reimbursement journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_20 = /*$$(*/
      "%s : You must configure a Sales journal type for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_21 = /*$$(*/
      "%s : You must configure a Credit note journal type for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_22 = /*$$(*/
      "%s : You must configure a Cash journal type for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_23 = /*$$(*/
      "%s : You must configure a Purchase journal type for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_24 = /*$$(*/
      "%s : You must configure an irrevocable doubtful account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_25 = /*$$(*/
      "%s : You must configure a customer account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_26 = /*$$(*/
      "%s : You must configure a supplier account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_27 = /*$$(*/
      "%s : You must configure a cash difference account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_28 = /*$$(*/
      "%s : You must configure a reimbursement account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_29 = /*$$(*/
      "%s : You must configure a doubtful customer account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_30 = /*$$(*/
      "%s : You must configure a direct debit payment mode for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_31 = /*$$(*/
      "%s : You must configure a payment mode after reject for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_32 = /*$$(*/
      "%s : You must configure a shift to irrecoverable's reason for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_34 = /*$$(*/
      "%s : You must configure a reject import letter template for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_35 = /*$$(*/
      "%s : You must configure a shifting reason (debt more than six months) for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_36 = /*$$(*/
      "%s : You must configure a shifting reason (debt more than three months) for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_37 = /*$$(*/
      "%s : You must configure a debt recovery tab for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_38 = /*$$(*/
      "%s : You must configure an advance payment account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_39 = /*$$(*/
      "%s : You must configure a file name for the export of move file for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_40 = /*$$(*/
      "%s : You must configure an employee account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_41 = /*$$(*/
      "%s : You must configure a factor credit account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_42 = /*$$(*/
      "%s : You must configure a factor debit account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_43 = /*$$(*/
      "%s : You must configure a year opening account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_44 = /*$$(*/
      "%s : You must configure a year closure account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_45 = /*$$(*/
      "%s : You must configure a reported balance journal for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_46 = /*$$(*/
      "%s : You must configure an supplier advance payment account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_47 = /*$$(*/
      "%s : You must configure a purchase financial discount tax for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_48 = /*$$(*/
      "%s : You must configure a sale financial discount tax for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_49 = /*$$(*/
      "%s : You must configure a purchase financial discount account for the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_50 = /*$$(*/
      "%s : You must configure a sale financial discount account for the company %s" /*)*/;

  public static final String ACCOUNT_CONFIG_SEQUENCE_1 = /*$$(*/
      "%s : Please, configure a sequence for the customer invoices and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_2 = /*$$(*/
      "%s : Please, configure a sequence for the customer refunds and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_3 = /*$$(*/
      "%s : Please, configure a sequence for the supplier invoices and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_4 = /*$$(*/
      "%s : Please, configure a sequence for the supplier refunds and the company %s" /*)*/;
  public static final String ACCOUNT_CONFIG_SEQUENCE_5 = /*$$(*/
      "%s : Please, configure a sequence for the fixed assets and the company %s" /*)*/;

  /** Cfonb config service */
  public static final String CFONB_CONFIG_1 = /*$$(*/
      "%s : You must configure CFONB for the company %s" /*)*/;

  public static final String CFONB_CONFIG_2 = /*$$(*/
      "%s : You must configure a CFONB sender code register for the company %s" /*)*/;
  public static final String CFONB_CONFIG_3 = /*$$(*/
      "%s : You must configure a CFONB sender's number for the company %s" /*)*/;
  public static final String CFONB_CONFIG_4 = /*$$(*/
      "%s : You must configure a CFONB sender's name/corporate name for the company %s" /*)*/;
  public static final String CFONB_CONFIG_5 = /*$$(*/
      "%s : You must configure a CFONB receiver code register for the company %s" /*)*/;
  public static final String CFONB_CONFIG_6 = /*$$(*/
      "%s : You must configure a CFONB total code register for the company %s" /*)*/;
  public static final String CFONB_CONFIG_7 = /*$$(*/
      "%s : You must configure a CFONB internet payment code for the company %s" /*)*/;
  public static final String CFONB_CONFIG_8 = /*$$(*/
      "%s : You must configure a CFONB direct debit code for the company %s" /*)*/;
  public static final String CFONB_CONFIG_9 = /*$$(*/
      "%s : You must configure a CFONB header code register for the company %s" /*)*/;
  public static final String CFONB_CONFIG_10 = /*$$(*/
      "%s : You must configure a CFONB detail code register for the company %s" /*)*/;
  public static final String CFONB_CONFIG_11 = /*$$(*/
      "%s : You must configure a CFONB code register end for the company %s" /*)*/;
  public static final String CFONB_CONFIG_12 = /*$$(*/
      "%s : You must configure a CFONB rejected direct debit code for the company %s" /*)*/;
  public static final String CFONB_CONFIG_13 = /*$$(*/
      "%s : You must configure a CFONB unpaid direct debit code fir the company %s" /*)*/;
  public static final String CFONB_CONFIG_14 = /*$$(*/
      "%s : You must configure a CFONB unpaid TIP code for the company %s" /*)*/;
  public static final String CFONB_CONFIG_15 = /*$$(*/
      "%s : You must configure a CFONB TIP and cheque TIP code for the company %s" /*)*/;
  public static final String CFONB_CONFIG_16 = /*$$(*/
      "%s : You must configure a CFONB TIP code for the company %s" /*)*/;

  /** Payer quality service */
  public static final String PAYER_QUALITY_1 = /*$$(*/
      "%s : Error : You must configure a weight table in general configuration" /*)*/;

  /** Debt recovery action service */
  public static final String DEBT_RECOVERY_ACTION_1 = /*$$(*/ "Debt recovery method missing." /*)*/;

  public static final String DEBT_RECOVERY_ACTION_2 = /*$$(*/ "Debt recovery line missing." /*)*/;
  public static final String DEBT_RECOVERY_ACTION_3 = /*$$(*/
      "%s : Letter template missing for debt recovery matrix %s (Partner %s, Level %s)." /*)*/;
  public static final String DEBT_RECOVERY_ACTION_4 = /*$$(*/
      "Email is not sent. Please check email account configuration." /*)*/;
  public static final String DEBT_RECOVERY_ACTION_5 = /*$$(*/
      "No email address in debt recovery %s. Please set one." /*)*/;

  /** Debt recovery service */
  public static final String DEBT_RECOVERY_1 = /*$$(*/ "There's no accounting situation." /*)*/;

  public static final String DEBT_RECOVERY_2 = /*$$(*/ "Reference date undefined." /*)*/;
  public static final String DEBT_RECOVERY_3 = /*$$(*/
      "Debt recovery method missing for the configuration." /*)*/;
  public static final String DEBT_RECOVERY_4 = /*$$(*/
      "Debt recovery level waiting for approval." /*)*/;
  public static final String DEBT_RECOVERY_DEBT_RECOVERY_LEVEL_NOT_FOUND = /*$$(*/
      "Debt recovery method line not found" /*)*/;

  /** Debt recovery session service */
  public static final String DEBT_RECOVERY_SESSION_1 = /*$$(*/
      "Debt recovery method line missing." /*)*/;

  /** Invoice batch service */
  public static final String INVOICE_BATCH_1 = /*$$(*/ "State %s unknown for treatment %s" /*)*/;

  /** Invoice generator */
  public static final String INVOICE_GENERATOR_1 = /*$$(*/
      "%s : Invoice's type is not filled %s" /*)*/;

  public static final String INVOICE_GENERATOR_2 = /*$$(*/ "%s : There's no partner selected" /*)*/;
  public static final String INVOICE_GENERATOR_3 = /*$$(*/ "%s : Payment condition missing" /*)*/;
  public static final String INVOICE_GENERATOR_4 = /*$$(*/ "%s : Payment mode missing" /*)*/;
  public static final String INVOICE_GENERATOR_5 = /*$$(*/ "%s : Invoicing address missing" /*)*/;
  public static final String INVOICE_GENERATOR_6 = /*$$(*/ "%s : Currency missing" /*)*/;

  /** Merge Invoice */
  public static final String INVOICE_MERGE_ERROR_CURRENCY = /*$$(*/
      "The currency is required and must be the same for all invoices" /*)*/;

  public static final String INVOICE_MERGE_ERROR_PARTNER = /*$$(*/
      "The partner is required and must be the same for all invoices" /*)*/;
  public static final String INVOICE_MERGE_ERROR_COMPANY = /*$$(*/
      "The company is required and must be the same for all invoices" /*)*/;
  public static final String INVOICE_MERGE_ERROR_SALEORDER = /*$$(*/
      "The sale order must be the same for all invoices" /*)*/;
  public static final String INVOICE_MERGE_ERROR_PURCHASEORDER = /*$$(*/
      "The purchase order must be the same for all invoices" /*)*/;
  public static final String INVOICE_MERGE_ERROR_PROJECT = /*$$(*/
      "The project must be the same for all invoices" /*)*/;
  public static final String INVOICE_MASS_PAYMENT_ERROR_PFP_LITIGATION = /*$$(*/
      "Their is at least one invoice selected that it is not validated to pay" /*)*/;

  /** Invoice line generator */
  public static final String INVOICE_LINE_GENERATOR_1 = /*$$(*/
      "You must select a currency for partner %s (%s)" /*)*/;

  public static final String INVOICE_LINE_GENERATOR_2 = /*$$(*/
      "You must select a currency for company %s" /*)*/;

  public static final String INVOICE_LINE_ERROR_FIXED_ASSET_CATEGORY = /*$$(*/
      "Fixed asset category is missing on invoice line for product %s" /*)*/;

  /** Batch validation */
  public static final String BATCH_VALIDATION_1 = /*$$(*/ "Invoice validation's reporting :" /*)*/;

  public static final String BATCH_VALIDATION_2 = /*$$(*/ "Invoice(s) validated" /*)*/;

  /** Batch ventilation */
  public static final String BATCH_VENTILATION_1 = /*$$(*/
      "Invoice ventilation's reporting :" /*)*/;

  public static final String BATCH_VENTILATION_2 = /*$$(*/ "Invoice(s) ventilated" /*)*/;

  /** Refund invoice */
  public static final String REFUND_INVOICE_1 = /*$$(*/
      "%s : Payment mode must be filled either in the partner or in the company configuration." /*)*/;

  /** Validate state */
  public static final String INVOICE_VALIDATE_1 = /*$$(*/
      "The payment mode is not in adequacy with the invoice type" /*)*/;

  public static final String INVOICE_VALIDATE_BLOCKING = /*$$(*/
      "The partner is blocked for invoicing." /*)*/;

  /** Cancel state */
  public static final String MOVE_CANCEL_1 = /*$$(*/
      "Move should be unreconcile before to cancel the invoice" /*)*/;

  public static final String MOVE_CANCEL_2 = /*$$(*/
      "Move is ventilated on a closed period, and can't be canceled" /*)*/;
  public static final String MOVE_CANCEL_3 = /*$$(*/
      "So many accounting operations are used on this move, so move can't be canceled" /*)*/;

  public static final String MOVE_CANCEL_4 = /*$$(*/
      "The move is accounted and so can not be canceled." /*)*/;

  public static final String INVOICE_CANCEL_1 = /*$$(*/
      "Invoice is passed in doubfult debit, and can't be canceled" /*)*/;

  public static final String INVOICE_PAYMENT_CANCEL = /*$$(*/
      "The bank order linked to this invoice payment has already been carried out/rejected, and thus can't be canceled" /*)*/;

  public static final String INVOICE_PAYMENT_NO_AMOUNT_REMAINING = /*$$(*/
      "The payment cannot be done because the amount remaining on the invoice %s is inferior or equal to 0." /*)*/;

  /** Ventilate state */
  public static final String VENTILATE_STATE_1 = /*$$(*/
      "Invoice's or credit note's date can't be previous last invoice ventilated's date : %s" /*)*/;

  public static final String VENTILATE_STATE_2 = /*$$(*/
      "Invoice's or credit note's date can't be previous last invoice ventilated on month's date : %s" /*)*/;
  public static final String VENTILATE_STATE_3 = /*$$(*/
      "Invoice's or credit note's date can't be previous last invoice ventilated on year's date : %s" /*)*/;
  public static final String VENTILATE_STATE_4 = /*$$(*/
      "Company %s does not have any invoice's nor credit note's sequence" /*)*/;
  public static final String VENTILATE_STATE_5 = /*$$(*/
      "The partner account can not be determined. Please set up the partner account on the invoice or configure the partner's accounting situation." /*)*/;
  public static final String VENTILATE_STATE_6 = /*$$(*/
      "The account of a product could not be determined or is not filled. Please fill the missing account on invoice line %s" /*)*/;
  public static final String VENTILATE_STATE_7 = /*$$(*/
      "An analytic distribution is set in product but the account used do not allow analytic distribution" /*)*/;

  public static final String VENTILATE_STATE_FUTURE_DATE = /*$$(*/
      "%s - Invoice date can't be in the future." /*)*/;

  public static final String VENTILATE_STATE_FUTURE_ORIGIN_DATE = /*$$(*/
      "Invoice date of origin can't be in the future." /*)*/;

  public static final String VENTILATE_STATE_MISSING_ORIGIN_DATE = /*$$(*/
      "Origin date is missing on the invoice" /*)*/;

  /** Workflow ventilation */
  public static final String AMOUNT_ADVANCE_PAYMENTS_TOO_HIGH = /*$$(*/
      "Sum of advance payments amounts is higher than the total of this invoice." /*)*/;

  public static final String PAYMENT_AMOUNT_EXCEEDING = /*$$(*/
      "%s : The paid amount is superior to the imputed amount(s)" /*)*/;

  /** Payment mode service */
  public static final String PAYMENT_MODE_1 = /*$$(*/ "Associated account not configured" /*)*/;

  public static final String PAYMENT_MODE_2 = /*$$(*/
      "%s : Error : You must configure a sequence for the company %s and a payment mode %s" /*)*/;
  public static final String PAYMENT_MODE_3 = /*$$(*/
      "%s : Error : You must configure a journal for the company %s and a payment mode %s" /*)*/;
  public static final String PAYMENT_MODE_4 = /*$$(*/
      "%s : Error : You must configure a bank details for the company %s and a payment mode %s" /*)*/;

  public static final String PAYMENT_MODE_ERROR_GETTING_ACCOUNT_FROM_PAYMENT_MODE = /*$$(*/
      "The configuration to retrieve the account on the payment mode is missing:" /*)*/;
  /** Payment voucher control service */
  public static final String PAYMENT_VOUCHER_CONTROL_PAID_AMOUNT = /*$$(*/
      "%s : Payment voucher n° %s, the paid amount should be positive" /*)*/;

  public static final String PAYMENT_VOUCHER_CONTROL_1 = /*$$(*/
      "%s : Caution, payment entry nb %s, total line's amount imputed is higher than customer's amount paid." /*)*/;
  public static final String PAYMENT_VOUCHER_CONTROL_2 = /*$$(*/
      "%s : There's no line to pay." /*)*/;
  public static final String PAYMENT_VOUCHER_CONTROL_3 = /*$$(*/
      "%s : You must add a journal and a treasury account into payment mode." /*)*/;

  /** Payment voucher load service */
  public static final String PAYMENT_VOUCHER_LOAD_1 = /*$$(*/
      "%s : You must add an amount paid." /*)*/;

  /** Payment voucher sequence service */
  public static final String PAYMENT_VOUCHER_SEQUENCE_1 = /*$$(*/
      "%s : You must configure a receipt number (Payment entry) for the company %s" /*)*/;

  /** Payment voucher tool service */
  public static final String PAYMENT_VOUCHER_TOOL_1 = /*$$(*/
      "Payment entry's type missing from payment entry %s" /*)*/;

  /** Payment voucher controller */
  public static final String PAYMENT_VOUCHER_REMOVE_NOT_OK = /*$$(*/
      "You can't remove this payment voucher as it is already used in a move." /*)*/;

  /** Payment schedule line service */
  public static final String PAYMENT_SCHEDULE_LINE_NO_DIRECT_DEBIT_PAYMENT_MODE = /*$$(*/
      "Missing direct debit payment mode in the company's account configuration" /*)*/;

  /** Account chart controller */
  public static final String ACCOUNT_CHART_1 = /*$$(*/
      "The chart of account has been loaded successfully" /*)*/;

  public static final String ACCOUNT_CHART_2 = /*$$(*/
      "Error in account chart import please check the log" /*)*/;
  public static final String ACCOUNT_CHART_3 = /*$$(*/
      "A chart or chart structure of accounts already exists, please delete the hierarchy between accounts in order to import a new chart." /*)*/;

  /** Address controller */
  public static final String ADDRESS_1 = /*$$(*/ "Sales map" /*)*/;

  public static final String ADDRESS_2 = /*$$(*/ "Not implemented for OSM" /*)*/;

  /** Invoice controller */
  public static final String INVOICE_1 = /*$$(*/ "Invoice canceled" /*)*/;

  public static final String INVOICE_2 = /*$$(*/ "Credit note created" /*)*/;
  public static final String INVOICE_3 = /*$$(*/ "Please select the invoice(s) to print." /*)*/;
  public static final String INVOICE_4 = /*$$(*/ "Refunds from invoice %s" /*)*/;

  public static final String INVOICE_NO_INVOICE_TO_PAY = /*$$(*/ "No invoice to pay" /*)*/;
  public static final String
      INVOICE_CAN_NOT_GO_BACK_TO_VALIDATE_STATUS_OR_CANCEL_VENTILATED_INVOICE = /*$$(*/
          "It is not possible to go back to validate status or cancel a ventilated invoice." /*)*/;

  public static final String INVOICE_CAN_NOT_DELETE = /*$$(*/
      "Invoices can only be deleted if they are canceled. %s can not be deleted." /*)*/;

  public static final String ACCOUNT_RECONCILABLE_USE_FOR_PARTNER_BALANCE = /*$$(*/
      "Please make sure that the customer account for the invoice is configured to be reconcilable and that it can be used for partner balance." /*)*/;

  /** Move template controller */
  public static final String MOVE_TEMPLATE_1 = /*$$(*/ "Template move is not balanced" /*)*/;

  public static final String MOVE_TEMPLATE_2 = /*$$(*/ "Error in move generation" /*)*/;
  public static final String MOVE_TEMPLATE_3 = /*$$(*/ "Generated moves" /*)*/;
  public static final String MOVE_TEMPLATE_4 = /*$$(*/ "Please fill input lines" /*)*/;

  /** Budget service */
  public static final String BUDGET_1 = /*$$(*/ "Too much iterations." /*)*/;

  public static final String EMPLOYEE_PARTNER = /*$$(*/
      "You must create a contact for employee %s" /*)*/;

  /*
   * Deposit slip
   */
  public static final String DEPOSIT_SLIP_MISSING_SEQUENCE = /*$$(*/
      "Missing deposit slip sequence for company %s" /*)*/;
  public static final String DEPOSIT_SLIP_CANNOT_DELETE = /*$$(*/
      "You cannot delete this deposit slip." /*)*/;
  public static final String DEPOSIT_SLIP_ALREADY_PUBLISHED = /*$$(*/
      "The deposit slip has already been published." /*)*/;
  public static final String DEPOSIT_SLIP_UNSUPPORTED_PAYMENT_MODE_TYPE = /*$$(*/
      "Unsupported payment mode type" /*)*/;

  /*
   * Partner
   */
  public static final String PARTNER_BANK_DETAILS_MISSING = /*$$(*/
      "Bank details are missing for partner %s." /*)*/;

  /*
   * Invoice printing
   */
  public static final String INVOICE_MISSING_PRINTING_SETTINGS = /*$$(*/
      "Please fill printing settings on invoice %s." /*)*/;
  public static final String INVOICES_MISSING_PRINTING_SETTINGS = /*$$(*/
      "Please fill printing settings on following invoices: %s" /*)*/;
  public static final String INVOICE_PRINTING_IO_ERROR = /*$$(*/
      "Error on uploading printed invoice:" /*)*/;

  /*
   * Reconcile Group
   */
  public static final String RECONCILE_GROUP_VALIDATION_NO_LINES = /*$$(*/
      "The reconcile group cannot be validated since there is no lines." /*)*/;
  public static final String RECONCILE_GROUP_NO_TEMP_SEQUENCE = /*$$(*/
      "There is no configured sequence for temporary reconcile group" /*)*/;
  public static final String RECONCILE_GROUP_NO_FINAL_SEQUENCE = /*$$(*/
      "There is no configured sequence for final reconcile group" /*)*/;

  /*
   * Subrogation Release
   */
  public static final String SUBROGATION_RELEASE_MISSING_SEQUENCE = /*$$(*/
      "Missing subrogation release sequence for company %s" /*)*/;
  public static final String SUBROGATION_RELEASE_SUBROGATION_ALREADY_EXIST_FOR_INVOICES = /*$$(*/
      "A transmitted or a accounted subrogation release already exist for the invoices %s." /*)*/;
  public static final String SUBROGATION_RELEASE_BACK_TO_ACCOUNTED_WRONG_STATUS = /*$$(*/
      "Can only go back to accounted from a cleared or cancelled subrogation release." /*)*/;

  /** MoveLine */
  public static final String NO_MOVE_LINE_SELECTED = /*$$(*/ "No Lines selected" /*)*/;

  /** User */
  public static final String USER_PFP_VALIDATOR_COMPANY_SET_NOT_EQUAL = /*$$(*/
      "%s has not exaclty the same internal companies as %s." /*)*/;

  public static final String USER_PFP_VALIDATOR_UPDATED = /*$$(*/
      "Pfp validator changed successfully" /*)*/;
  public static final String USER_PFP_VALIDATOR_NO_RELATED_ACCOUNTING_SITUATION = /*$$(*/
      "No Accounting Situation related to %s." /*)*/;

  /* Check refunds */
  public static final String INVOICE_NOT_IMPUTED_CLIENT_REFUNDS = /*$$(*/
      "Note: there are existing not imputed client refunds."; /*)*/
  public static final String INVOICE_NOT_IMPUTED_SUPPLIER_REFUNDS = /*$$(*/
      "Note: there are existing not imputed supplier refunds."; /*)*/

  public static final String FIXED_ASSET_DISPOSAL_DATE_ERROR_1 = /*$$(*/
      "Disposal date must be after the date of the last depreciation." /*)*/;
  public static final String FIXED_ASSET_DISPOSAL_DATE_ERROR_2 = /*$$(*/
      "Disposal date shouldn't be after the next planned depreciation date. Please realize all depreciations that happened before the disposal." /*)*/;
  public static final String FIXED_ASSET_DISPOSAL_DATE_YEAR_ALREADY_ACCOUNTED = /*$$(*/
      "The disposal of the asset cannot be executed while depreciation has already been accounted." /*)*/;

  /* MOVE REVERSE*/
  public static final String REVERSE_DATE_SELECT_UNKNOW_TYPE = /*$$(*/
      "There is no reverse date select value of value %d" /*)*/;

  /*Check not lettered advance move lines*/
  public static final String INVOICE_NOT_LETTERED_SUPPLIER_ADVANCE_MOVE_LINES = /*$$(*/
      "There is at least one advance payment or payment that can be imputed to this invoice." /*)*/;

  public static final String CLOSE_NO_REPORTED_BALANCE_DATE = /*$$(*/
      "Please set a reported balance date on fiscal year" /*)*/;

  public static final String ACCOUNT_CODE_ALREADY_IN_USE_FOR_COMPANY = /*$$(*/
      "The account code %s is already used for the company %s, there cannot be two accounts with the same code for the same company." /*)*/;;

  public static final String INVALID_ANALYTIC_MOVE_LINE = /*$$(*/
      "Invalid Analytic moveLines, some axes percentage values are different than 100%." /*)*/;

  /*Close annual account batch */
  public static final String BATCH_CLOSE_ANNUAL_ACCOUNT_1 = /*$$(*/
      "%s : Error : You must configure accounts for the batch configurator %s" /*)*/;

  public static final String BATCH_CLOSE_ANNUAL_ACCOUNT_2 = /*$$(*/
      "%s : Error : You must configure a year for the batch configurator %s" /*)*/;

  public static final String BATCH_DOES_NOT_EXIST = /*$$(*/ "The batch does not exist." /*)*/;

  public static final String BATCH_BLOCK_CUSTOMER_WITH_LATE_PAYMENT_MISSING = /*$$(*/
      "Please set up an accounting batch to block customers with late payments" /*)*/;

  public static final String ACCOUNT_PERIOD_TEMPORARILY_CLOSED = /*$$(*/
      "The period of the move %s is temporarily closed and you do not have the necessary permissions to edit moves" /*)*/;

  public static final String ANALYTIC_DISTRIBUTION_TEMPLATE_CHECK_COMPANY_AXIS_AND_JOURNAL = /*$$(*/
      "Selected AnalyticAxis and AnalyticJournal doesn't belong to the select company." /*)*/;

  public static final String ANALYTIC_DISTRIBUTION_TEMPLATE_CHECK_COMPANY_AXIS = /*$$(*/
      "Selected AnalyticAxis doesn't belong to the select company." /*)*/;

  public static final String ANALYTIC_DISTRIBUTION_TEMPLATE_CHECK_COMPANY_JOURNAL = /*$$(*/
      "Selected AnalyticJournal doesn't belong to the select company." /*)*/;

  public static final String MOVE_CHECK_ORIGIN = /*$$(*/
      "The move field origin is empty, do you wish to continue ?" /*)*/;

  public static final String MOVE_CHECK_DESCRIPTION = /*$$(*/
      "The move field description is empty, do you wish to continue ?" /*)*/;

  public static final String DATE_NOT_IN_PERIOD_MOVE = /*$$(*/
      "The date input on the move line of %s %s on account %s is not belonging to the accounting period defined on the move." /*)*/;

  public static final String DATE_NOT_IN_PERIOD_MOVE_WITHOUT_ACCOUNT = /*$$(*/
      "The selected date is out of the accounting period." /*)*/;

  /* FEC IMPORT */
  public static final String CAN_NOT_IMPORT_MOVE_ALREADY_EXIST = /*$$(*/
      "The import failed: the move %s already exist." /*)*/;

  public static final String FIXED_ASSET_ANALYTIC_ACCOUNT_MISSING = /*$$(*/
      "Please fill analytic accounts in all analytic distribution lines." /*)*/;

  /* Analytic axis */
  public static final String NOT_UNIQUE_CODE_ANALYTIC_AXIS_WITH_COMPANY = /*$$(*/
      "The code defined here is already used by another record for the specified %s. Code must be unique by company. Please modify it accordingly." /*)*/;
  /* Analytic axis */
  public static final String NOT_UNIQUE_CODE_ANALYTIC_AXIS_NULL_COMPANY = /*$$(*/
      "The code defined here is already used by another record. Code must be unique either by company or if shared. Please modify it accordingly." /*)*/;

  /*Period */
  public static final String PERIOD_DIFFERENTS_DATE_WHEN_NOT_OPENED = /*$$(*/
      "This period is already in use by some accounting moves. The dates can't be modified." /*)*/;

  /*Period */
  public static final String FISCAL_YEARS_DIFFERENTS_DATE_WHEN_NOT_OPENED = /*$$(*/
      "This fiscal year contains period which are already in use by some accounting moves. The dates can't be modified anymore." /*)*/;

  public static final String INACTIVE_ACCOUNT_FOUND =
      /*$$(*/ "The following account within the moveLine : %s is inactive. Thus, the move can't be set to accounted" /*)*/;

  public static final String INACTIVE_ACCOUNTS_FOUND =
      /*$$(*/ "The following accounts within the moveLine : %s are inactive. Thus, the move can't be set to accounted" /*)*/;

  public static final String INACTIVE_ANALYTIC_JOURNAL_FOUND = /*$$(*/
      "The following analytic journal : %s is inactive and linked to a move line. Thus, the move can't be set to daybook/accounted." /*)*/;

  public static final String INACTIVE_ANALYTIC_JOURNALS_FOUND = /*$$(*/
      "The following analytic journals : %s are inactive and linked to a move line. Thus, the move can't be set to daybook/accounted." /*)*/;

  public static final String INACTIVE_ANALYTIC_ACCOUNT_FOUND =
      /*$$(*/ "The following analytic account : %s is inactive and linked to a move line. Thus, the move can't be set to daybook/accounted." /*)*/;

  public static final String INACTIVE_ANALYTIC_ACCOUNTS_FOUND =
      /*$$(*/ "The following analytic accounts : %s are inactive and linked to a move line. Thus, the move can't be set to daybook/accounted." /*)*/;

  public static final String INACTIVE_JOURNAL_FOUND =
      /*$$(*/ "The journal : %s is inactive. Thus, the move can't be set to daybook/accounted." /*)*/;

  // Split message
  public static final String SPLIT_MESSAGE_COMMENT = /*$$(*/ "Split of %.2f realized on %s" /*)*/;

  public static final String BATCH_BILL_OF_EXCHANGE_ACCOUNT_MISSING = /*$$(*/
      "Account '%s' is missing in account config" /*)*/;

  public static final String NOTE_BILLS_CONFIG_SEQUENCE = /*$$(*/
      "%s : Please, configure a sequence for the note bills and the company %s" /*)*/;

  public static final String FIXED_ASSET_GROSS_VALUE_0 = /*$$(*/
      "The gross value of a fixed asset must be greater than zero. The fixed asset %s can't be validated." /*)*/;

  public static final String EXCEPTION_GENERATE_COUNTERPART = /*$$(*/
      "Please select a payment mode to generate the counterpart" /*)*/;

  public static final String FIXED_ASSET_SEQUENCE_ALREADY_EXISTS = /*$$(*/
      "A sequence already exists on this code for this company and this sequence have to be unique" /*)*/;

  public static final String FIXED_ASSET_PARTIAL_TO_TOTAL_DISPOSAL = /*$$(*/
      "The quantity selected is the same as that of the fixed asset. The disposal will therefore be treated as a total disposal" /*)*/;

  public static final String IMPORT_FEC_ACCOUNT_NOT_FOUND = /*$$(*/
      "The account with the code %s cannot be found. The move line cannot be created." /*)*/;

  public static final String IMPORT_FEC_JOURNAL_NOT_FOUND = /*$$(*/
      "The journal with the code %s cannot be found. The move cannot be created." /*)*/;

  public static final String IMPORT_FEC_PERIOD_NOT_FOUND = /*$$(*/
      "No period found for the date %s and the company %s. The move cannot be created." /*)*/;

  public static final String ACCOUNT_MANAGEMENT_CASH_ACCOUNT_MISSING_PAYMENT = /*$$(*/
      "Please select a cash account in config of the payment mode %s" /*)*/;

  public static final String MASS_UPDATE_SUCCESSFUL =
      /*$$(*/ "Operation successful : %s record(s) updated." /*)*/;

  public static final String MASS_UPDATE_SELECTED_NO_RECORD =
      /*$$(*/ "No record has been updated. Please make sure the selection can be updated accordingly." /*)*/;

  public static final String MASS_UPDATE_ALL_NO_RECORD =
      /*$$(*/ "No record has been updated. Please make sure the selection can be updated accordingly (e.g. Status different from selected one)." /*)*/;

  public static final String MASS_UPDATE_NO_RECORD_SELECTED = /*$$(*/
      "No record has been selected" /*)*/;

  public static final String MASS_UPDATE_NO_STATUS = /*$$(*/ "Please select a status." /*)*/;
  public static final String CAPITAL_DEPRECIATION_DEROGATORY_ACCOUNT = /*$$(*/
      "Capital Depreciation Derogatory Account" /*)*/;
  public static final String EXPENSE_DEPRECIATION_DEROGATORY_ACCOUNT = /*$$(*/
      "Expense Depreciation Derogatory Account" /*)*/;
  public static final String INCOME_DEPRECIATION_DEROGATORY_ACCOUNT = /*$$(*/
      "Income Depreciation Derogatory Account" /*)*/;

  public static final String MOVE_PERIOD_IS_CLOSED = /*$$(*/
      "The period of the move is closed or temporary closed and can not be accounted" /*)*/;

  public static final String MOVE_14 = /*$$(*/
      "The functional origin %s of the account move %s is not allowed on the journal %s (%s)" /*)*/;

  public static final String MOVE_INCONSISTENCY_DETECTED_JOURNAL_COMPANY = /*$$(*/
      "Inconsistency detected as the company defined on the move %s is different from the company associated to the journal %s." /*)*/;

  public static final String MOVE_LINE_INCONSISTENCY_DETECTED_MOVE_COMPANY_ACCOUNT_COMPANY = /*$$(*/
      "Inconsistency detected as the company defined on the move %s is different from the company associated to the accounts on the move line(s)." /*)*/;

  public static final String
      MOVE_LINE_INCONSISTENCY_DETECTED_JOURNAL_COMPANY_ACCOUNT_COMPANY = /*$$(*/
          "Inconsistency detected as the company defined on the journal %s is different from the company associated to the accounts on the move line(s)." /*)*/;

  public static final String ACCOUNT_CONFIG_MISSING_CASH_POSITION_VARIATION_ACCOUNT = /*$$(*/
      "You must configure an account for cashier regulation." /*)*/;

  public static final String MOVE_INVOICE_DESCRIPTION_REQUIRED = /*$$(*/
      "Description is required in moves for company %s but description of moves that are being generated will be empty. Please make sure a journal is set and fill a default description or enable document number to be used as such." /*)*/;

  public static final String
      IMMO_FIXED_ASSET_DISPOSAL_COMPANY_ACCOUNT_CONFIG_CUSTOMER_SALES_JOURNAL_EMPTY = /*$$(*/
          "The company account configuration customer sales journal is required." /*)*/;
}
