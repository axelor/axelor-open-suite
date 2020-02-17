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
package com.axelor.apps.bankpayment.report;

public interface ITranslation {

  public static final String EBICS_CERTIFICATE_SIGNATURE_TITLE = /*$$(*/
      "EbicsCertificateReport.signatureTitle"; /*)*/
  public static final String EBICS_CERTIFICATE_AUTHENTICATION_TITLE = /*$$(*/
      "EbicsCertificateReport.authenticationTitle"; /*)*/
  public static final String EBICS_CERTIFICATE_ENCRYPTION_TITLE = /*$$(*/
      "EbicsCertificateReport.encryptionTitle"; /*)*/
  public static final String EBICS_CERTIFICATE_SIGNATURE = /*$$(*/
      "EbicsCertificateReport.signature"; /*)*/
  public static final String EBICS_CERTIFICATE_AUTHENTICATION = /*$$(*/
      "EbicsCertificateReport.authentication"; /*)*/
  public static final String EBICS_CERTIFICATE_ENCRYPTION = /*$$(*/
      "EbicsCertificateReport.encryption"; /*)*/
  public static final String EBICS_CERTIFICATE_SIGNATURE_HASH = /*$$(*/
      "EbicsCertificateReport.signatureHash"; /*)*/
  public static final String EBICS_CERTIFICATE_AUTHENTICATION_HASH = /*$$(*/
      "EbicsCertificateReport.authenticationHash"; /*)*/
  public static final String EBICS_CERTIFICATE_ENCRYPTION_HASH = /*$$(*/
      "EbicsCertificateReport.encryptionHash"; /*)*/
  public static final String EBICS_CERTIFICATE_ISSUED_TO = /*$$(*/
      "EbicsCertificateReport.issuedTo"; /*)*/
  public static final String EBICS_CERTIFICATE_ISSUED_BY = /*$$(*/
      "EbicsCertificateReport.issuedBy"; /*)*/
  public static final String EBICS_CERTIFICATE_BANK = /*$$(*/ "EbicsCertificateReport.bank"; /*)*/
  public static final String EBICS_CERTIFICATE_SIGNATURE_VERSION = /*$$(*/
      "EbicsCertificateReport.signatureVersion"; /*)*/
  public static final String EBICS_CERTIFICATE_AUTHENTICATION_VERSION = /*$$(*/
      "EbicsCertificateReport.authenticationVersion"; /*)*/
  public static final String EBICS_CERTIFICATE_ENCRYPTION_VERSION = /*$$(*/
      "EbicsCertificateReport.encryptionVersion"; /*)*/

  public static final String BANK_STATEMENT_TITLE = /*$$(*/ "BankStatement.title"; /*)*/;
  public static final String BANK_STATEMENT_CREATION_DATE_FROM = /*$$(*/
      "BankStatement.creationDateFrom"; /*)*/;
  public static final String BANK_STATEMENT_CREATION_DATE_TO = /*$$(*/
      "BankStatement.creationDateTo"; /*)*/;
  public static final String BANK_STATEMENT_HEADER_TEXT = /*$$(*/ "BankStatement.headerText"; /*)*/;
  public static final String BANK_STATEMENT_OPERATION_DATE = /*$$(*/
      "BankStatement.operationDate"; /*)*/;
  public static final String BANK_STATEMENT_VALUE_DATE = /*$$(*/ "BankStatement.valueDate"; /*)*/;
  public static final String BANK_STATEMENT_LABEL = /*$$(*/ "BankStatement.label"; /*)*/;
  public static final String BANK_STATEMENT_REFERENCE = /*$$(*/ "BankStatement.reference"; /*)*/;
  public static final String BANK_STATEMENT_EXEMPT = /*$$(*/ "BankStatement.exempt"; /*)*/;
  public static final String BANK_STATEMENT_UNAVAILABLE = /*$$(*/
      "BankStatement.unavailable"; /*)*/;
  public static final String BANK_STATEMENT_ORIGIN = /*$$(*/ "BankStatement.origin"; /*)*/;
  public static final String BANK_STATEMENT_DEBIT = /*$$(*/ "BankStatement.debit"; /*)*/;
  public static final String BANK_STATEMENT_CREDIT = /*$$(*/ "BankStatement.credit"; /*)*/;
  public static final String BANK_STATEMENT_YES = /*$$(*/ "BankStatement.yes"; /*)*/
  public static final String BANK_STATEMENT_INITIAL_BALANCE = /*$$(*/
      "BankStatement.initialBalance"; /*)*/;
  public static final String BANK_STATEMENT_MOVEMENT = /*$$(*/ "BankStatement.movement"; /*)*/;
  public static final String BANK_STATEMENT_FINAL_BALANCE = /*$$(*/
      "BankStatement.finalBalance"; /*)*/;
  public static final String BANK_STATEMENT_TOTAL_OF_OPERATIONS = /*$$(*/
      "BankStatement.totalOfOperations"; /*)*/;
  public static final String BANK_STATEMENT_BANK_ACCOUNT = /*$$(*/
      "BankStatement.bankAccount"; /*)*/;
  public static final String BANK_STATEMENT_IBAN = /*$$(*/ "BankStatement.iban"; /*)*/;
  public static final String BANK_STATEMENT_ACCOUNT_CURRENCY = /*$$(*/
      "BankStatement.accountCurrency"; /*)*/;

  public static final String BANK_RECONCILIATION_REPORT_EDITION_DATE = /*$$(*/
      "BankReconciliation.report_edition_date"; /*)*/;
  public static final String BANK_RECONCILIATION_PAGE1_TITLE = /*$$(*/
      "BankReconciliation.page1Title"; /*)*/;
  public static final String BANK_RECONCILIATION_PAGE2_TITLE = /*$$(*/
      "BankReconciliation.page2Title"; /*)*/;
  public static final String BANK_RECONCILIATION_ACOUNT = /*$$(*/
      "BankReconciliation.account"; /*)*/;
  public static final String BANK_RECONCILIATION_JOURNAL = /*$$(*/
      "BankReconciliation.journal"; /*)*/;
  public static final String BANK_RECONCILIATION_CURRENCY = /*$$(*/
      "BankReconciliation.currency"; /*)*/;
  public static final String BANK_RECONCILIATION_COMPANY = /*$$(*/
      "BankReconciliation.company"; /*)*/;
  public static final String BANK_RECONCILIATION_REPORT_DATE = /*$$(*/
      "BankReconciliation.report_date"; /*)*/;
  public static final String BANK_RECONCILIATION_CASH_ACCOUNT = /*$$(*/
      "BankReconciliation.cash_account"; /*)*/;

  public static final String BANK_RECONCILIATION_TYPE = /*$$(*/ "BankReconciliation.type"; /*)*/;
  public static final String BANK_RECONCILIATION_CODE = /*$$(*/ "BankReconciliation.code"; /*)*/;
  public static final String BANK_RECONCILIATION_TO_DATE = /*$$(*/
      "BankReconciliation.to_date"; /*)*/;
  public static final String BANK_RECONCILIATION_REFERENCE = /*$$(*/
      "BankReconciliation.reference"; /*)*/;
  public static final String BANK_RECONCILIATION_NAME = /*$$(*/ "BankReconciliation.name"; /*)*/;
  public static final String BANK_RECONCILIATION_DEBIT = /*$$(*/ "BankReconciliation.debit"; /*)*/;
  public static final String BANK_RECONCILIATION_CREDIT = /*$$(*/
      "BankReconciliation.credit"; /*)*/;
  public static final String BANK_RECONCILIATION_AMOUNT = /*$$(*/
      "BankReconciliation.amount"; /*)*/;
  public static final String BANK_RECONCILIATION_UNRECONCILED_MOVE_LINES = /*$$(*/
      "BankReconciliation.unreconciled_move_lines"; /*)*/;
  public static final String BANK_RECONCILIATION_MOVES_UNACCOUNTED = /*$$(*/
      "BankReconciliation.moves_unaccounted"; /*)*/;

  public static final String BANK_RECONCILIATION_STATEMENT_COMPANY = /*$$(*/
      "BankReconciliationStatement.company"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_CURRENCY = /*$$(*/
      "BankReconciliationStatement.currency"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_JOURNAL = /*$$(*/
      "BankReconciliationStatement.journal"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_DATE = /*$$(*/
      "BankReconciliationStatement.date"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_PAGE1TITLE = /*$$(*/
      "BankReconciliationStatement.page1Title"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_PAGE2TITLE = /*$$(*/
      "BankReconciliationStatement.page2Title"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_CODE = /*$$(*/
      "BankReconciliationStatement.code"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_REFERENCE = /*$$(*/
      "BankReconciliationStatement.reference"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_DESCRIPTION = /*$$(*/
      "BankReconciliationStatement.description"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_DEBIT = /*$$(*/
      "BankReconciliationStatement.debit"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_CREDIT = /*$$(*/
      "BankReconciliationStatement.credit"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_AMOUNT_REMAINING = /*$$(*/
      "BankReconciliationStatement.amountRemaining"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_NAME = /*$$(*/
      "BankReconciliationStatement.name"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_ACCOUNT_CODE = /*$$(*/
      "BankReconciliationStatement.accountCode"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_JOURNAL_CODE = /*$$(*/
      "BankReconciliationStatement.journalCode"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_LINE = /*$$(*/
      "BankReconciliationStatement.line"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_AMOUNT_TO_RECONCILE = /*$$(*/
      "BankReconciliationStatement.amountToReconcile"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_NO_LINE_FOUND_MESSAGE = /*$$(*/
      "BankReconciliationStatement.noLineFoundMessage"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_ACCOUNT = /*$$(*/
      "BankReconciliationStatement.account"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_BANK_DETAILS = /*$$(*/
      "BankReconciliationStatement.bankDetails"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_ACTUAL_BALANCE = /*$$(*/
      "BankReconciliationStatement.actualBalance"; /*)*/;
  public static final String BANK_RECONCILIATION_STATEMENT_MAIN_TITLE = /*$$(*/
      "BankReconciliationStatement.mainTitle"; /*)*/;
}
