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
package com.axelor.apps.budget.exception;

public final class BudgetExceptionMessage {

  private BudgetExceptionMessage() {}

  public static final String MISSING_ADVANCED_EXPORT = /*$$(*/
      "Missing required advanced export(s)." /*)*/;

  public static final String BUDGET_IS_MISSING = /*$$(*/ "Please select a budget with Id." /*)*/;

  public static final String MISSING_ACCOUNTS_IN_COMPANY = /*$$(*/
      "Error : Following accounts are not found %s" /*)*/;

  public static final String BUDGET_DISTRIBUTION_LINE_SUM_GREATER_INVOICE = /*$$(*/
      "The budget distribution amount for budget %s exceeds the amount of the invoice line %s, please correct it" /*)*/;

  public static final String BUDGET_DISTRIBUTION_LINE_SUM_GREATER_MOVE = /*$$(*/
      "The budget distribution amount for budget %s exceeds the amount of the move line %s, please correct it" /*)*/;

  public static final String BUDGET_DISTRIBUTION_LINE_SUM_GREATER_PO = /*$$(*/
      "The budget distribution amount for budget %s exceeds the amount of the order line %s, please correct it" /*)*/;

  public static final String ERROR_CONFIG_BUDGET_KEY = /*$$(*/
      "When budget key is enabled, you must check at least one line on analytic axis to be included in budget key computation" /*)*/;

  public static final String BUDGET_ANALYTIC_EMPTY = /*$$(*/
      "The budget key is enabled in the company %s account configuration. Thus, you must fill the analytic axis and the analytic account before validating in order to ensure its generation (Budget %s)" /*)*/;

  public static final String BUDGET_ACCOUNT_EMPTY = /*$$(*/
      "The budget key is enabled in the company %s account configuration. Thus, you must fill at least one accounting account before validating in order to ensure its generation (Budget %s)" /*)*/;

  public static final String BUDGET_SAME_BUDGET_KEY = /*$$(*/
      "There is already a budget key using the same combination of company, dates, accounts and analytic accounts and axis than the budget line %s" /*)*/;

  public static final String BUDGET_KEY_NOT_FOUND = /*$$(*/
      "No budget could be reconciled with the data entered for following lines : %s" /*)*/;

  public static final String BUDGET_MISSING_BUDGET_KEY = /*$$(*/
      "The budget key is missing in budget %s. Please fill account and analytic distribution configuration in budget before validating in order to ensure its generation" /*)*/;

  public static final String BUDGET_EXCEED_ORDER_LINE_AMOUNT = /*$$(*/
      "The budget distribution amount exceed the amount on the order line with product %s, please correct it" /*)*/;

  public static final String BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST = /*$$(*/
      "You can't compute the budget distribution because you are not authorized to." /*)*/;

  public static final String NO_BUDGET_VALUES_FOUND = /*$$(*/
      "The budget distribution has not been computed yet. By launching this action, you can no longer compute the budget distribution." /*)*/;

  public static final String WRONG_DATES_ON_BUDGET = /*$$(*/
      "Please select valid dates for budget %s, dates needs to be in the section period" /*)*/;

  public static final String WRONG_DATES_ON_BUDGET_LINE = /*$$(*/
      "Please select valid dates for budget lines in budget %s, dates need to be in the budget period" /*)*/;

  public static final String BUDGET_LINES_ON_SAME_PERIOD = /*$$(*/
      "Please select valid dates for budget lines in budget %s, budget lines need to be on a separate period" /*)*/;

  public static final String WRONG_DATES_ON_BUDGET_LEVEL = /*$$(*/
      "Please select valid dates for budget level %s, dates needs to be in the parent period" /*)*/;

  public static final String ADVANCED_IMPORT_IMPORT_DATA = /*$$(*/
      "Data imported successfully" /*)*/;

  public static final String NO_BUDGET_VALUES_FOUND_ERROR = /*$$(*/
      "Budget distribution has not been configured. It is required for the rest of the process." /*)*/;

  public static final String BUGDET_EXCEED_ERROR = /*$$(*/
      "There is a budget overrun for %s. The available balance is %.2f %s."; /*)*/

  public static final String BUDGET_EXCEED_ERROR_ALERT = /*$$(*/
      "If you click OK you will be in excess, do you want to continue ?"; /*)*/

  public static final String BUDGET_1 = /*$$(*/ "Too much iterations." /*)*/;

  public static final String BUDGET_VARIABLE = /*$$(*/ "Budget Scenario Variable %s" /*)*/;

  public static final String BUDGET_DISTRIBUTION_LINE_SUM_LINES_GREATER_INVOICE = /*$$(*/
      "The sum of the budget distribution amounts exceeds the amount of the invoice line %s, please correct it" /*)*/;

  public static final String BUDGET_DISTRIBUTION_LINE_SUM_LINES_GREATER_PO = /*$$(*/
      "The sum of the budget distribution amount for budget exceeds the amount of the order line %s, please correct it" /*)*/;

  public static final String BUDGET_DISTRIBUTION_LINE_SUM_LINES_GREATER_MOVE = /*$$(*/
      "The sum of the budget distribution amount for budget exceeds the amount of the move line %s, please correct it" /*)*/;
}
