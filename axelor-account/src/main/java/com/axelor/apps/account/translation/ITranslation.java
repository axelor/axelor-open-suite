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
package com.axelor.apps.account.translation;

public interface ITranslation {

  public static final String ACCOUNTING_APP_NAME = /*$$(*/ "value:Accounting"; /*)*/
  public static final String INVOICING_APP_NAME = /*$$(*/ "value:Invoicing"; /*)*/
  public static final String ACCOUNT_DEBIT_BALANCE = /*$$(*/ "Debit balance" /*)*/;
  public static final String ACCOUNT_CREDIT_BALANCE = /*$$(*/ "Credit balance" /*)*/;
  public static final String INVOICE_LINE_END_OF_PACK = /*$$(*/ "InvoiceLine.endOfPack" /*)*/;
  public static final String INVOICE_LINE_TOTAL = /*$$(*/ "InvoiceLine.total" /*)*/;
  public static final String INVOICE_PURCHASE_SITUTATION_INVOICED_AMOUNT = /*$$(*/
      "Invoiced Amount" /*)*/;
  public static final String INVOICE_PURCHASE_SITUTATION_UNPAID_INVOICE_AMOUNT = /*$$(*/
      "Unpaid Inv. due" /*)*/;
  public static final String PAYMENT_SESSION_COMPUTE_NAME_ON_THE = /*$$(*/ "on the" /*)*/;
  public static final String PAYMENT_SESSION_COMPUTE_NAME_BY = /*$$(*/ "by" /*)*/;
  public static final String FIXED_ASSET_SPLIT_AMOUNT = /*$$(*/ "fixedAsset.amount" /*)*/;

  public static final String FISCAL_YEAR_CODE = /*$$(*/ "fiscalYear.code" /*)*/;
  public static final String FIXED_ASSET_IMPORT_BTN_IMPORT = /*$$(*/
      "Define origin as Imported/Transferred" /*)*/;
  public static final String FIXED_ASSET_IMPORT_BTN_MANUAL = /*$$(*/ "Reset origin" /*)*/;
  public static final String CLOSURE_OPENING_BATCH_DAYBOOK_MOVE_ERROR_LABEL =
      /*$$(*/ "The fiscal year %s still contains %s entry/ies in Daybook which may subsequently lead to inconsistencies with the calculations of the balances to be carried forward with this closure process if they are modified. We recommand to post those entries or close definitely the periods before proceeding with the closure. Do you still wish to continue ?" /*)*/;
}
