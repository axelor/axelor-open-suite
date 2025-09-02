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
package com.axelor.apps.businessproject.exception;

public final class BusinessProjectExceptionMessage {

  private BusinessProjectExceptionMessage() {}

  public static final String FOLDER_TEMPLATE = /*$$(*/ "You must add a sale order template" /*)*/;
  public static final String INVOICING_PROJECT_EMPTY = /*$$(*/
      "You haven't select any element to invoice" /*)*/;
  public static final String INVOICING_PROJECT_PROJECT = /*$$(*/
      "You must select a project/task" /*)*/;
  public static final String INVOICING_PROJECT_PROJECT_PARTNER = /*$$(*/
      "There is no customer for this project/task" /*)*/;
  public static final String INVOICING_PROJECT_PROJECT_PRODUCT = /*$$(*/
      "You haven't select a product to invoice for the task %s" /*)*/;
  public static final String INVOICING_PROJECT_PROJECT_COMPANY = /*$$(*/
      "You haven't select a company on the main project" /*)*/;
  public static final String FACTORY_NO_FOUND = /*$$(*/
      "Factory not found this type of generator" /*)*/;
  public static final String FACTORY_FILL_WITH_PROJECT_ALONE = /*$$(*/
      "You can't fill a project with the strategy Project Alone." /*)*/;
  public static final String LINES_NOT_SELECTED = /*$$(*/ "Please select lines" /*)*/;

  public static final String SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_1 = /*$$(*/
      "Products must be Service type and Method of Supply Produce." /*)*/;
  public static final String SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_2 = /*$$(*/
      "Please complete the order lines with at least one product type 'Service' and the supply mode 'Produce'" /*)*/;
  public static final String SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_3 = /*$$(*/
      "Nothing has changed so no new task will be generated" /*)*/;

  public static final String INVALID_EXCLUDE_TASK_FILTER = /*$$(*/
      "Invalid exclude task for invoicing filter" /*)*/;

  public static final String BATCH_TASK_UPDATION_1 = /*$$(*/ "Task %s" /*)*/;

  public static final String BATCH_TASK_UPDATION_2 = /*$$(*/ "Tasks update completed: " /*)*/;

  public static final String BATCH_TIMESHEETLINE_UPDATION_1 = /*$$(*/ "Timesheet line %s" /*)*/;

  public static final String BATCH_INVOICING_PROJECT_1 = /*$$(*/ "Project %s" /*)*/;

  public static final String BATCH_INVOICING_PROJECT_2 = /*$$(*/
      "Generated invoicing project" /*)*/;

  public static final String BATCH_COMPUTE_PROJECT_TOTALS_1 = /*$$(*/ "Project %s" /*)*/;

  public static final String BATCH_COMPUTE_PROJECT_TOTALS_2 = /*$$(*/
      "Compute project totals" /*)*/;

  public static final String SALE_ORDER_GENERATE_FILL_PRODUCT_UNIT_ERROR = /*$$(*/
      "%s is in %s and should be in Days or Hours as they are defined in Business Project module configuration" /*)*/;

  public static final String PROJECT_UPDATE_TOTALS_SUCCESS = /*$$(*/
      "Totals have been updated" /*)*/;

  public static final String PROJECT_TASK_PRODUCT_SALE_ORDER_LINE_UNIT_ERROR = /*$$(*/
      "The order line unit of the product %s is not compatible with the configuration." /*)*/;

  public static final String PROJECT_TASK_PRODUCT_STOCK_UNIT_ERROR = /*$$(*/
      "The storage unit of the product %s is not compatible with the configuration." /*)*/;

  public static final String PROJECT_TASK_SOLD_TIME_ERROR = /*$$(*/
      "Sold time value error for project task %s." /*)*/;

  public static final String PROJECT_TASK_UPDATE_REPORTING_VALUES_ERROR = /*$$(*/
      "Sold time and updated time must be greater than 0 for task %s" /*)*/;

  public static final String PROJECT_TASK_NO_PROJECT_FOUND = /*$$(*/
      "No project found for task %s." /*)*/;

  public static final String BATCH_BACKUP_TO_PROJECT_HISTORY = /*$$(*/
      "Backup project data to project history" /*)*/;
  public static final String BATCH_BACKUP_TO_PROJECT_HISTORY_ERROR = /*$$(*/
      "Error while trying to save to project history for project %s." /*)*/;

  public static final String PROJECT_REPORT_NO_ID_FOUND = /*$$(*/
      "Could not find the project id." /*)*/;

  public static final String PROJECT_TASK_REPORT_NO_ID_FOUND = /*$$(*/
      "Could not find the project task id." /*)*/;

  public static final String PROJECT_TASK_TEMPLATE_QUANTITY_ERROR = /*$$(*/
      "Quantity must be greater than 0 for task template %s." /*)*/;

  public static final String PROJECT_TASK_FRAMEWORK_CONTRACT_PRODUCT_NOT_FOUND = /*$$(*/
      "The product was not found on the linked framework contract." /*)*/;

  public static final String PROJECT_TASK_FRAMEWORK_CONTRACT_PRODUCT_NB_ERROR = /*$$(*/
      "Impossible to retrieve the price as several matching products were found. " /*)*/;

  public static final String PROJECT_CLOSING_BLOCKING_MESSAGE = /*$$(*/
      "You can not close this project :" /*)*/;

  public static final String PROJECT_CLOSING_NON_BLOCKING_MESSAGE = /*$$(*/ "WARNING:" /*)*/;

  public static final String PROJECT_CLOSING_SALE_ORDER_NOT_INVOICED = /*$$(*/
      "- a linked sale order is not totally invoiced" /*)*/;

  public static final String PROJECT_CLOSING_PURCHASE_ORDER_NOT_INVOICED = /*$$(*/
      "- a linked purchase order is not totally invoiced" /*)*/;

  public static final String PROJECT_CLOSING_PURCHASE_ORDER_NOT_RECEIVED = /*$$(*/
      "- a linked purchase order is not totally received" /*)*/;

  public static final String PROJECT_CLOSING_CONTRACT_IN_PROGRESS = /*$$(*/
      "- a linked contract is still in progress" /*)*/;

  public static final String PROJECT_CLOSING_TIMESHEET_LINE_NOT_INVOICED = /*$$(*/
      "- a linked timesheet line is not invoiced" /*)*/;

  public static final String PROJECT_CLOSING_EXPENSE_LINE_NOT_INVOICED = /*$$(*/
      "- a linked expense line is not invoiced" /*)*/;

  public static final String PROJECT_EMPLOYEE_PRODUCT_NOT_IN_CONTRACT = /*$$(*/
      "The timesheet product defined on the employee is not available on contracts linked to the task." /*)*/;

  /** Business Project Config Service */
  public static final String BUSINESS_PROJECT_CONFIG_1 = /*$$(*/
      "%s : You must configure Business project module for company %s" /*)*/;

  public static final String BATCH_TRANSITION_BETWEEN_PAID_STATUS = /*$$(*/
      "Transition between paid status for projects" /*)*/;
  public static final String BATCH_TRANSITION_BETWEEN_PAID_STATUS_ERROR = /*$$(*/
      "Error while trying to transition between paid status for project %s." /*)*/;

  public static final String PROJECT_BUSINESS_PROJECT_MISSING_CLIENT_PARTNER_COMPANY = /*$$(*/
      "Please fill the company and the client partner before conversion." /*)*/;
}
