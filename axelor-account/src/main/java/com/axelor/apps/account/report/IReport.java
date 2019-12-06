/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.report;

public interface IReport {

  public static final String ACCOUNTING_REPORT_TYPE = "AccountingReportType%s.rptdesign";
  public static final String PAYMENT_VOUCHER = "PaymentVoucher.rptdesign";
  public static final String IRRECOVERABLE = "Irrecoverable.rptdesign";
  public static final String INVOICE = "Invoice.rptdesign";
  public static final String SALE_INVOICES_DETAILS = "SaleInvoicesDetails.rptdesign";
  public static final String PURCHASE_INVOICES_DETAILS = "PurchaseInvoicesDetails.rptdesign";
  public static final String ACCOUNT_MOVE = "AccountMove.rptdesign";
  public static final String SUBROGATION_RELEASE = "SubrogationRelease.rptdesign";
  public static final String CHEQUE_DEPOSIT_SLIP = "ChequeDepositSlip.rptdesign";
  public static final String CASH_DEPOSIT_SLIP = "CashDepositSlip.rptdesign";
  public static final String DEBT_RECOVERY = "PaymentReminder.rptdesign";
}
