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
package com.axelor.apps.hr.exception;

public final class HumanResourceExceptionMessage {

  private HumanResourceExceptionMessage() {}

  public static final String HR_CONFIG = /*$$(*/
      "Please configure information for human resources for the company %s" /*)*/;
  public static final String HR_CONFIG_EXPENSE_TYPE = /*$$(*/
      "Please configure the expense type for kilometric allowance in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_SENT_EXPENSE_TEMPLATE = /*$$(*/
      "Please configure the sent expense template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_VALIDATED_EXPENSE_TEMPLATE = /*$$(*/
      "Please configure the validated expense template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_REFUSED_EXPENSE_TEMPLATE = /*$$(*/
      "Please configure the refused expense template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_CANCELED_EXPENSE_TEMPLATE = /*$$(*/
      "Please configure the canceled expense template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_SENT_TIMESHEET_TEMPLATE = /*$$(*/
      "Please configure the sent timehsheet template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_VALIDATED_TIMESHEET_TEMPLATE = /*$$(*/
      "Please configure the validated timehsheet template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_REFUSED_TIMESHEET_TEMPLATE = /*$$(*/
      "Please configure the refused timehsheet template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_CANCELED_TIMESHEET_TEMPLATE = /*$$(*/
      "Please configure the canceled timehsheet template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_SENT_LEAVE_TEMPLATE = /*$$(*/
      "Please configure the sent leave template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_VALIDATED_LEAVE_TEMPLATE = /*$$(*/
      "Please configure the validated leave template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_REFUSED_LEAVE_TEMPLATE = /*$$(*/
      "Please configure the refused leave template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_CANCELED_LEAVE_TEMPLATE = /*$$(*/
      "Please configure the canceled leave template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_SENT_EXTRA_HOURS_TEMPLATE = /*$$(*/
      "Please configure the sent extra hours template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_VALIDATED_EXTRA_HOURS_TEMPLATE = /*$$(*/
      "Please configure the validated extra hours template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_REFUSED_EXTRA_HOURS_TEMPLATE = /*$$(*/
      "Please configure the refused extra hours template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_CANCELED_EXTRA_HOURS_TEMPLATE = /*$$(*/
      "Please configure the canceled extra hours template in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_LEAVE_REASON = /*$$(*/
      "Please configure the unjustified absence reason in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_LUNCH_VOUCHER_EXPORT_PATH = /*$$(*/
      "Please configure the lunch voucher export path in HR config for the company %s" /*)*/;
  public static final String HR_CONFIG_NO_EXPENSE_SEQUENCE = /*$$(*/
      "Company %s does not have any expense's sequence" /*)*/;
  public static final String HR_CONFIG_FORMULA_VARIABLE_MISSING = /*$$(*/
      "Please configure Formula Variables for human resource for the company %s" /*)*/;
  public static final String EXPENSE_NO_COMPANY_BANK_DETAILS = /*$$(*/
      "Default bank details are not filled for your company." /*)*/;

  public static final String TIMESHEET_FROM_DATE = /*$$(*/
      "Please add a start date for generation" /*)*/;
  public static final String TIMESHEET_TO_DATE = /*$$(*/
      "Please add an end date for generation" /*)*/;
  public static final String TIMESHEET_PRODUCT = /*$$(*/ "Please add a product" /*)*/;
  public static final String TIMESHEET_EMPLOYEE_DAY_PLANNING = /*$$(*/
      "Please add an employee's planning related to user %s" /*)*/;
  public static final String TIMESHEET_EMPLOYEE_DAILY_WORK_HOURS = /*$$(*/
      "Please, enter the number of daily work hours per employee %s" /*)*/;
  public static final String TIMESHEET_DAILY_WORK_HOURS = /*$$(*/
      "Please, configure the number of daily work hours." /*)*/;

  public static final String TIMESHEET_TIME_LOGGING_PREFERENCE = /*$$(*/
      "Please, configure the time logging unit preference." /*)*/;
  public static final String PROJECT_PLANNING_WRONG_TIME_UNIT = /*$$(*/
      "Project planning time unit not matching with time units in base configuration." /*)*/;
  public static final String TIMESHEET_NULL_FROM_DATE = /*$$(*/ "From date can't be empty" /*)*/;
  public static final String TIMESHEET_NULL_TO_DATE = /*$$(*/ "To date can't be empty" /*)*/;
  public static final String TIMESHEET_LINE_NULL_DATE = /*$$(*/
      "The date of timesheet line %d can't be empty" /*)*/;
  public static final String GENERAL_EMPLOYEE_ACTIVITY = /*$$(*/
      "Please, enter an activity for the employee %s" /*)*/;
  public static final String TIMESHEET_EMPLOYEE_PUBLIC_HOLIDAY_EVENTS_PLANNING = /*$$(*/
      "Please add an employee's public holiday events planning related to user %s" /*)*/;
  public static final String TIMESHEET_TIMESHEET_LINE_LIST_IS_EMPTY = /*$$(*/
      "Timesheet line list is empty, please add a timesheet line list" /*)*/;
  public static final String TIMESHEET_HOLIDAY = /*$$(*/ "Holiday" /*)*/;
  public static final String TIMESHEET_DAY_LEAVE = /*$$(*/ "Day leave" /*)*/;

  public static final String LEAVE_USER_EMPLOYEE = /*$$(*/
      "Please create an employee for the user %s" /*)*/;
  public static final String LEAVE_LINE = /*$$(*/
      "There is no leave line for the employee %s and the reason %s." /*)*/;
  public static final String LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE = /*$$(*/
      "Employee %s is not allowed to take leave in advance." /*)*/;
  public static final String LEAVE_ALLOW_NEGATIVE_VALUE_REASON = /*$$(*/
      "You are not able to take leave in advance for the reason '%s'." /*)*/;
  public static final String LEAVE_ALLOW_NEGATIVE_ALERT = /*$$(*/
      "You now have a negative number of leaves available for the reason %s" /*)*/;
  public static final String LEAVE_ALLOW_NEGATIVE_ALERT_2 = /*$$(*/
      "You will have a negative number of leaves available." /*)*/;
  public static final String LEAVE_REASON_NO_UNIT = /*$$(*/
      "Please, choose unit in leave reason %s." /*)*/;
  public static final String LEAVE_REQUEST_DATES_OVERLAPPED = /*$$(*/
      "A leave request is already accepted over this or a portion of this period of time. Please cancel the previous one to accept this one." /*)*/;
  public static final String LEAVE_REQUEST_NO_COMPANY = /*$$(*/
      "Please set a company up on leave request" /*)*/;
  public static final String LEAVE_REQUEST_NO_LINE_PRESENT = /*$$(*/
      "You need lines to fill your export." /*)*/;
  public static final String INVALID_DATES = /*$$(*/ "Invalid dates" /*)*/;
  public static final String LEAVE_REQUEST_WRONG_DURATION = /*$$(*/ "Duration equals 0" /*)*/;

  public static final String EMPLOYEE_PLANNING = /*$$(*/
      "Please, add a planning for employee : %s" /*)*/;
  public static final String EMPLOYEE_PUBLIC_HOLIDAY = /*$$(*/
      "Please, add a public holiday planning for employee : %s" /*)*/;
  public static final String EMPLOYEE_CONTRACT_OF_EMPLOYMENT = /*$$(*/
      "Please, add a contract of employment for employee : %s" /*)*/;

  public static final String BATCH_MISSING_FIELD = /*$$(*/
      "Leave reason and day number have to be defined" /*)*/;
  public static final String EMPLOYEE_DOUBLE_LEAVE_MANAGEMENT = /*$$(*/
      "The employee %s has multiple %s leave lines" /*)*/;
  public static final String EMPLOYEE_NO_LEAVE_MANAGEMENT = /*$$(*/
      "The employee %s has no %s leave line" /*)*/;
  public static final String EMPLOYEE_NO_SENIORITY_DATE = /*$$(*/
      "The employee %s has no seniority date" /*)*/;
  public static final String EMPLOYEE_NO_BIRTH_DATE = /*$$(*/
      "The employee %s has no birth date" /*)*/;
  public static final String CONTACT_CANNOT_DELETE = /*$$(*/
      "This contact is referenced from employee %s - %s" /*)*/;

  public static final String BATCH_LEAVE_MANAGEMENT_ENDING_0 = /*$$(*/
      "Employees' leaves attempted to be computed : %s" /*)*/;
  public static final String BATCH_LEAVE_MANAGEMENT_ENDING_1 = /*$$(*/
      "Employees' leaves successfully computed : %s" /*)*/;
  public static final String BATCH_LEAVE_MANAGEMENT_ENDING_2 = /*$$(*/
      "Employees' leaves failed to be computed due to configuration anomaly : %s" /*)*/;
  public static final String BATCH_LEAVE_MANAGEMENT_ENDING_3 = /*$$(*/
      "Employees' leaves failed to be computed due to missing data : %s" /*)*/;
  public static final String BATCH_LEAVE_MANAGEMENT_QTY_OUT_OF_BOUNDS = /*$$(*/
      "Qty must be lower than %d." /*)*/;
  public static final String BATCH_SENIORITY_LEAVE_MANAGEMENT_FORMULA = /*$$(*/
      "There is an error in a formula" /*)*/;
  public static final String BATCH_PAYROLL_PREPARATION_GENERATION_RECAP = /*$$(*/
      "Payroll preparations attempted to be generated : %s" /*)*/;
  public static final String BATCH_PAYROLL_PREPARATION_SUCCESS_RECAP = /*$$(*/
      "Payroll preparations successfully generated : %s" /*)*/;
  public static final String BATCH_PAYROLL_PREPARATION_DUPLICATE_RECAP = /*$$(*/
      "Payroll preparations failed to be generated due to a duplicate one : %s" /*)*/;
  public static final String BATCH_PAYROLL_PREPARATION_CONFIGURATION_RECAP = /*$$(*/
      "Payroll preparations failed to be generated due to missing data : %s" /*)*/;
  public static final String BATCH_PAYROLL_PREPARATION_EXPORT_RECAP = /*$$(*/
      "Payroll preparations exported : %s" /*)*/;

  public static final String BATCH_TIMESHEET_MISSING_TEMPLATE = /*$$(*/
      "You must choose a template." /*)*/;
  public static final String BATCH_TIMESHEET_REMINDER_DONE = /*$$(*/ "Employees computed: %d" /*)*/;
  public static final String BATCH_TIMESHEET_REMINDER_ANOMALY = /*$$(*/
      "Employees failed to be computed due to anomaly: %d" /*)*/;

  public static final String BATCH_CREDIT_TRANSFER_EXPENSE_DONE_SINGULAR = /*$$(*/
      "%d expense treated successfully," /*)*/;
  public static final String BATCH_CREDIT_TRANSFER_EXPENSE_DONE_PLURAL = /*$$(*/
      "%d expenses treated successfully," /*)*/;

  public static final String LUNCH_VOUCHER_MIN_STOCK = /*$$(*/
      "Minimum stock of lunch vouchers will be reached for the company %s. Minimum Stock allowed : %s. Available Stock : %s" /*)*/;

  public static final String KILOMETRIC_LOG_NO_YEAR = /*$$(*/
      "There is no year for society %s which includes date %s" /*)*/;

  public static final String KILOMETRIC_LOG_NO_CIVIL_YEAR = /*$$(*/
      "There is no civil year for society %s which includes date %s" /*)*/;

  public static final String KILOMETRIC_LOG_NO_FISCAL_YEAR = /*$$(*/
      "There is no fiscal year for society %s which includes date %s" /*)*/;

  public static final String KILOMETRIC_LOG_NO_PAYROLL_YEAR = /*$$(*/
      "There is no payroll for society %s which includes date %s" /*)*/;

  public static final String KILOMETRIC_ALLOWANCE_NO_RULE = /*$$(*/
      "There is no matching condition for the allowance %s" /*)*/;
  public static final String KILOMETRIC_ALLOWANCE_NO_DATE_SELECTED = /*$$(*/
      "There is no year selected for the allowance." /*)*/;

  public static final String PAYROLL_PREPARATION_DUPLICATE = /*$$(*/
      "There is already a payroll preparation for the employee %s, the company %s and the period %s" /*)*/;

  /** Expense service */
  public static final String EXPENSE_JOURNAL = /*$$(*/
      "You must configure an expenses journal(company : %s)" /*)*/;

  public static final String EXPENSE_ACCOUNT = /*$$(*/
      "You must configure an expenses account (company : %s)" /*)*/;
  public static final String EXPENSE_ACCOUNT_TAX = /*$$(*/
      "You must configure an account for expenses taxes (company : %s)" /*)*/;
  public static final String EXPENSE_CANCEL_MOVE = /*$$(*/
      "Move already used, you must unreconcile it first" /*)*/;

  public static final String EXPENSE_TAX_PRODUCT = /*$$(*/ "No Tax for the product %s" /*)*/;
  public static final String EXPENSE_MISSING_PERIOD = /*$$(*/ "Please fill the period" /*)*/;
  public static final String EXPENSE_CLOSED_PERIOD = /*$$(*/ "Period is closed" /*)*/;
  public static final String EXPENSE_MISSING_PAYMENT_MODE = /*$$(*/
      "Please fill the payment mode." /*)*/;
  public static final String ALREADY_INVITED_TO_RESTAURANT = /*$$(*/
      "You have already been invited to the restaurant for the following dates:" /*)*/;

  /** Timesheet Editor */
  public static final String NEW_PROJECT_LINE = /*$$(*/ "New project line" /*)*/;

  /** Kilometric allowance */
  public static final String KILOMETRIC_ALLOWANCE_GOOGLE_MAPS_ERROR = /*$$(*/
      "Google Maps error: %s" /*)*/;

  public static final String KILOMETRIC_ALLOWANCE_OSM_ERROR = /*$$(*/
      "Open Street Maps error: %s" /*)*/;

  public static final String EXPENSE_PAYMENT_CANCEL = /*$$(*/
      "The bank order linked to this expense has already been carried out/rejected, and thus can't be canceled" /*)*/;

  /** Kilometric service */
  public static final String KILOMETRIC_ALLOWANCE_RATE_MISSING = /*$$(*/
      "The kilometric allowance rate corresponding to the kilometric allow param %s and the company %s is missing" /*)*/;

  /** TsTimer Service */
  public static final String NO_TIMESHEET_CREATED = /*$$(*/
      "No timesheet line has been created because the duration is less than 1 minute" /*)*/;

  public static final String EXPENSE_NOT_SELECTED = /*$$(*/ "Please, select an expense" /*)*/;

  public static final String BATCH_EMPLOYMENT_CONTRACT_EXPORT_RECAP = /*$$(*/
      "Employment contracts exported : %s" /*)*/;

  public static final String UNIT_SELECT_FOR_LEAVE_REASON = /*$$(*/
      "Please configure the unit for this type of absence" /*)*/;

  public static final String EMPLOYEE_TIMESHEET_REMINDER_TEMPLATE = /*$$(*/
      "Please configure the template for email reminder" /*)*/;

  public static final String NO_TIMESHEET_FOUND_FOR_EMPLOYEE = /*$$(*/
      "No time sheet found for employee %s" /*)*/;

  public static final String NO_USER_FOR_EMPLOYEE = /*$$(*/
      "Please fill a user for the employee %s" /*)*/;

  public static final String EXPENSE_CAN_NOT_DELETE_VENTILATED = /*$$(*/
      "Ventilated expense can not be deleted. %s can not be deleted." /*)*/;

  public static final String EXPENSE_LINE_DATE_ERROR = /*$$(*/ "Date can't be in the future" /*)*/;

  public static final String EXPENSE_LINE_MISSING_EXPENSE_PRODUCT = /*$$(*/
      "Please provide an expense product." /*)*/;

  public static final String EXPENSE_LINE_MISSING_KILOMETRIC_ALLOWANCE_PARAM = /*$$(*/
      "Please provide a kilometric allowance parameter." /*)*/;

  public static final String EXPENSE_LINE_MISSING_KILOMETRIC_TYPE = /*$$(*/
      "Please provide a kilometric type." /*)*/;

  public static final String EXPENSE_LINE_MISSING_CITIES = /*$$(*/
      "Please provide starting and arriving cities." /*)*/;

  public static final String EXPENSE_LINE_MISSING_COMPANY = /*$$(*/
      "Please provide a company." /*)*/;

  public static final String MEDICAL_VISIT_PLAN_WRONG_STATUS = /*$$(*/
      "Can only plan draft medical visit." /*)*/;

  public static final String MEDICAL_VISIT_REALIZE_WRONG_STATUS = /*$$(*/
      "Can only realize planned medical visit." /*)*/;

  public static final String MEDICAL_VISIT_CANCEL_WRONG_STATUS = /*$$(*/
      "Can only cancel planned or realized medical visit." /*)*/;

  public static final String EXPENSE_BIRT_TEMPLATE_MISSING = /*$$(*/
      "Please configure a birt template for expense report." /*)*/;

  public static final String EXPENSE_ADD_LINE_WRONG_STATUS = /*$$(*/
      "Can only add expense line to drafted expense." /*)*/;

  public static final String EXPENSE_LINE_VALIDATE_TOTAL_AMOUNT = /*$$(*/
      "The expense line amount exceeds the authorized limit of %s. Please correct the amount or request the intervention of an HR manager." /*)*/;

  public static final String EXPENSE_LINE_CURRENCY_NOT_EQUAL = /*$$(*/
      "Not all expenses are in the same currency. You can only create or add lines to an expense report when they have the same currency." /*)*/;

  public static final String EXPENSE_LINE_JUSTIFICATION_FILE_NOT_CORRECT_FORMAT = /*$$(*/
      "The file is not a PDF nor an image. Please keep the original document." /*)*/;

  public static final String EXPENSE_JUSTIFICATION_FILE_MISSING = /*$$(*/
      "Some lines do not have a justification file. Do you want to proceed ?" /*)*/;

  public static final String EXPENSE_LIMIT_EXCEEDED = /*$$(*/
      "The expense limit has been exceeded for the period %s to %s" /*)*/;

  public static final String EXPENSE_LINE_NO_PROJECT = /*$$(*/
      "This expense cannot be created because no project has been filled in and it has been noted as to be billed." /*)*/;

  public static final String EXPENSE_LINE_UPDATE_BILLING_INCOMPATIBLE_PROJECT = /*$$(*/
      "Expense line has been set as to be billed but billing expenses is disabled on this project." /*)*/;

  public static final String TIMESHEET_LINES_EXCEED_DAILY_LIMIT = /*$$(*/
      "You can't exceed the daily limit of %s hours on the %s." /*)*/;

  public static final String EXPENSE_LINE_EXPENSE_TYPE_NOT_ALLOWED = /*$$(*/
      "Only HR manager can choose this expense type." /*)*/;

  public static final String EXPENSE_AMOUNT_LIMIT_ERROR = /*$$(*/
      "At least one line of this expense exceeds the authorised amount for its type of expense." /*)*/;

  public static final String EXPENSE_LINE_DISTANCE_ERROR = /*$$(*/
      "This is a kilometric expense line. Distance must be greater than 0." /*)*/;

  public static final String EXPENSE_LINE_NO_LINE_SELECTED = /*$$(*/
      "Please select at least one expense line." /*)*/;

  public static final String EXPENSE_LINE_SELECTED_CURRENCY_ERROR = /*$$(*/
      "All lines must have the same currency." /*)*/;

  public static final String EXPENSE_LINE_SELECTED_EMPLOYEE_ERROR = /*$$(*/
      "All lines must have the same employee." /*)*/;

  public static final String TIMESHEET_TIMER_EMPTY_EMPLOYEE = /*$$(*/
      "Please choose an employee." /*)*/;

  public static final String TIMESHEET_TIMER_EMPTY_ACTIVITY = /*$$(*/
      "Please choose an activity." /*)*/;

  public static final String TIMESHEET_TIMER_EMPTY_PROJECT_OR_TASK = /*$$(*/
      "Project and project task must be chosen together." /*)*/;

  public static final String TIMESHEET_TIMER_PROJECT_TASK_INCONSISTENCY = /*$$(*/
      "Given project task and project are not related." /*)*/;

  public static final String TIMESHEET_TIMER_ACTIVITY_INCONSISTENCY = /*$$(*/
      "Given project task and activity are not related." /*)*/;

  public static final String TIMESHEET_TIMER_TIMER_STOP_CONFIG_DISABLED = /*$$(*/
      "Editing timer on stop need to be enabled to set a duration." /*)*/;

  public static final String TIMESHEET_TIMER_USER_NO_EMPLOYEE = /*$$(*/
      "Current user doesn't have an employee." /*)*/;

  public static final String TIMESHEET_TIMER_ALREADY_STARTED = /*$$(*/
      "Timer is already ongoing, stop it before starting a new one." /*)*/;

  public static final String TIMESHEET_ADD_TIMER_WRONG_STATUS = /*$$(*/
      "You can only add a timer to a drafted or a waiting for validation timesheet." /*)*/;

  public static final String TIMESHEET_LINE_INVALID_DATE = /*$$(*/
      "This date is invalid. It must be included in the timesheet's period." /*)*/;

  public static final String TIMESHEET_CONFIRM_COMPLETE_WRONG_STATUS = /*$$(*/
      "Can only confirm/complete from draft status." /*)*/;

  public static final String TIMESHEET_VALIDATE_WRONG_STATUS = /*$$(*/
      "Can only validate a waiting for validation timesheet." /*)*/;

  public static final String TIMESHEET_REFUSE_WRONG_STATUS = /*$$(*/
      "Can only refuse a waiting for validation timesheet." /*)*/;

  public static final String TIMESHEET_CANCEL_WRONG_STATUS = /*$$(*/
      "You can not cancel an already cancelled timesheet." /*)*/;

  public static final String TIMESHEET_VALIDATION_NEEDED_NOT_ENABLED = /*$$(*/
      "Validation needed configuration is not enabled." /*)*/;

  public static final String TIMESHEET_ACTIVITY_NOT_ENABLED = /*$$(*/
      "Activity on timesheet line is not enabled." /*)*/;

  public static final String TIMESHEET_ACTIVITY_NOT_ALLOWED = /*$$(*/
      "The activity is not allowed in the current project." /*)*/;
  public static final String TIMESHEET_PRODUCT_NOT_ACTIVITY = /*$$(*/
      "The given product is not an activity, please choose a correct activity." /*)*/;

  public static final String EXPENSE_LINE_PARENT_NOT_DRAFT = /*$$(*/
      "This expense line is related to an expense which is not in draft." /*)*/;

  public static final String EXPENSE_LINE_NEW_EXPENSE_NOT_DRAFT = /*$$(*/
      "The new expense is not in draft." /*)*/;

  public static final String EXPENSE_LINE_EXPENSE_NOT_DRAFT = /*$$(*/
      "You can not update a line from an expense which is not in draft." /*)*/;

  public static final String EXPENSE_LINE_UPDATED_CURRENCY_INCONSISTENCY = /*$$(*/
      "Updated currency and new parent currency are incompatible." /*)*/;

  public static final String EXPENSE_LINE_UPDATED_CURRENCY_CURRENT_EXPENSE_INCONSISTENCY = /*$$(*/
      "Updated currency and current parent currency are incompatible." /*)*/;

  public static final String TIMESHEET_INVALID_DATES = /*$$(*/
      "The start date cannot be more recent than the end date." /*)*/;

  public static final String TIMESHEET_LINE_INVALID_DATES = /*$$(*/
      "%s date is invalid. It must be included in the timesheet's period." /*)*/;

  public static final String NO_TIMESHEET_LINE_GENERATED = /*$$(*/
      "No timesheet lines can be generated because no duration is entered. Please fill the duration field if you want to generate timesheet lines." /*)*/;

  public static final String NO_TIMESHEET_GENERATED_DATE = /*$$(*/
      "Please fill a generation date." /*)*/;

  public static final String DATE_NOT_IN_TIMESHEET_PERIOD = /*$$(*/
      "The date must be in the timesheet period." /*)*/;

  public static final String API_LEAVE_REQUEST_WRONG_START_ON_SELECT = /*$$(*/
      "startOnSelect should be 1 for morning or 2 for afternoon." /*)*/;

  public static final String API_LEAVE_REQUEST_NONE_CREATED = /*$$(*/
      "No leave requests were created." /*)*/;

  public static final String PROJECT_PLANNING_TIME_FIRST_REQUEST = /*$$(*/
      "No project planning time exists for this task. Do you want to create new project planning lines?" /*)*/;
  public static final String PROJECT_PLANNING_TIME_NEW_REQUEST = /*$$(*/
      "Warning: Project planning time already exists for this task but can't be changed automatically. Do you want to create new project planning lines?" /*)*/;

  public static final String PROJECT_PLANNING_TIME_EXISTING_ON_OLD_SPRINT = /*$$(*/
      "Project planning time on the old sprint's dates are existing, do you want to move those to the new period ?" /*)*/;

  public static final String PROJECT_PLANNING_TIME_EXISTING_WITH_OLD_DURATION = /*$$(*/
      "Project planning time on the sprint dates are existing, do you want to update these with the new budgeted time?" /*)*/;

  public static final String LEAVE_REQUEST_NOT_ENOUGH_DAYS = /*$$(*/
      "You will not have enough leaves available for this request." /*)*/;

  public static final String TIMESHEET_CREATE_NO_USER_ERROR = /*$$(*/
      "No user was found when creating the timesheet." /*)*/;
}
