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
package com.axelor.apps.hr.translation;

public interface ITranslation {

  public static final String EMPLOYEES_MANAGEMENT_APP_NAME = /*$$(*/
      "value:Employees Management"; /*)*/
  public static final String EXTRA_HOURS_APP_NAME = /*$$(*/ "value:Extra hours"; /*)*/
  public static final String EXPENSE_MANAGEMENT_APP_NAME = /*$$(*/ "value:Expense Management"; /*)*/
  public static final String TIMESHEET_MANAGEMENT_APP_NAME = /*$$(*/
      "value:Timesheet Management"; /*)*/
  public static final String LEAVE_MANAGEMENT_APP_NAME = /*$$(*/ "value:Leave Management"; /*)*/

  public static final String PUBLIC_HOLIDAY_TITLE = /*$$(*/ "Public holidays"; /*)*/
  public static final String WEEKLY_PLANNING_TITLE = /*$$(*/ "6 days week"; /*)*/

  public static final String TS_REPORT_FILL_NO_EMPLOYEE = /*$$(*/ "No employee found"; /*)*/
  public static final String TS_REPORT_TITLE = /*$$(*/ "TimesheetReport"; /*)*/

  public static final String REQUEST_OVERFLOW = /*$$(*/ "Too many requests"; /*)*/
  public static final String NO_SUCH_PLACE = /*$$(*/ "No such place exists"; /*)*/
  public static final String NO_ROUTE = /*$$(*/ "No Route Found"; /*)*/

  public static final String EMPLOYEE_CONTRACT = /*$$(*/ "Employee.contract"; /*)*/
  public static final String EMPLOYEE_RESUME = /*$$(*/ "Employee.resume"; /*)*/
  public static final String EMPLOYEE_PERSONAL_INFORMATIONS = /*$$(*/
      "Employee.personalInformations"; /*)*/
  public static final String EMPLOYEE_POSITION_LIST = /*$$(*/ "Employee.positionList"; /*)*/

  public static final String EMPLOYEE_DAILY_COST = /*$$(*/ "Employee.dailyCost"; /*)*/
  public static final String EMPLOYEE_ENTRY_DATE = /*$$(*/ "Employee.entryDate"; /*)*/
  public static final String EMPLOYEE_DEPARTURE_DATE = /*$$(*/ "Employee.departureDate"; /*)*/
  public static final String EMPLOYEE_DEPARTMENT = /*$$(*/ "Employee.department"; /*)*/
  public static final String EMPLOYEE_START_DATE = /*$$(*/ "Employee.startDate"; /*)*/
  public static final String EMPLOYEE_END_DATE = /*$$(*/ "Employee.endDate"; /*)*/
  public static final String EMPLOYEE_WEEKLY_WORK = /*$$(*/ "Employee.weeklyWork"; /*)*/
  public static final String EMPLOYEE_DAILY_WORK = /*$$(*/ "Employee.dailyWork"; /*)*/

  public static final String EMPLOYEE_DOB = /*$$(*/ "Employee.dob"; /*)*/
  public static final String EMPLOYEE_COUNTRY = /*$$(*/ "Employee.country"; /*)*/
  public static final String EMPLOYEE_MARTIAL_STATUS = /*$$(*/ "Employee.martialStatus"; /*)*/
  public static final String EMPLOYEE_PHONE_AT_CUSTOMER = /*$$(*/ "Employee.phoneAtCustomer"; /*)*/
  public static final String EMPLOYEE_EMERGENCY_CONTACT = /*$$(*/ "Employee.emergencyContact"; /*)*/
  public static final String EMPLOYEE_EMERGENCY_NUMBER = /*$$(*/ "Employee.emergencyNumber"; /*)*/

  public static final String EMPLOYEE_COMPANY = /*$$(*/ "Employee.company"; /*)*/
  public static final String EMPLOYEE_POSITION = /*$$(*/ "Employee.position"; /*)*/
  public static final String EMPLOYEE_CONTRACT_TYPE = /*$$(*/ "Employee.contractType"; /*)*/

  public static final String EMPLOYEE_CODE = /*$$(*/ "Employee code"; /*)*/
  public static final String EMPLOYEE_CODE_NATURE = /*$$(*/ "Code nature"; /*)*/
  public static final String EMPLOYEE_NAME_AND_SURNAME = /*$$(*/ "Name and surname"; /*)*/
  public static final String EMPLOYEE_LUNCH_VOUCHER_NUMBER = /*$$(*/ "Lunch Voucher's number"; /*)*/
  public static final String LEAVE_REQUEST_START_DATE = /*$$(*/ "Start date"; /*)*/
  public static final String LEAVE_REQUEST_END_DATE = /*$$(*/ "End date"; /*)*/
  public static final String LEAVE_REQUEST_START_ON = /*$$(*/ "Start On"; /*)*/
  public static final String LEAVE_REQUEST_END_ON = /*$$(*/ "End On"; /*)*/

  public static final String EDITOR_TEAM_TASK = /*$$(*/ "TeamTask"; /*)*/
  public static final String EDITOR_ADD_A_LINE = /*$$(*/ "ADD A LINE"; /*)*/

  String MEDICAL_VISIT = /*$$(*/ "Medical visit"; /*)*/

  String SET_TOTAL_TAX_ZERO = /*$$(*/
      "The total tax has been set to zero since the expense type blocks taxes."; /*)*/

  String DISTANCE_BETWEEN_CITIES = /*$$(*/ "The distance between %s and %s." /*)*/;
  public static final String INCREMENT_LEAVE_REASON_BATCH_EXECUTION_RESULT = /*$$(*/
      "%d leave reason(s) treated and %d anomaly(ies) reported !"; /*)*/

  public static final String CHECK_RESPONSE_RESPONSE = /*$$(*/
      "Here is the list of the warnings and errors" /*)*/;
  public static final String EXPENSE_LINE_UPDATED = /*$$(*/
      "Expense line successfully updated." /*)*/;
  public static final String EXPENSE_UPDATED = /*$$(*/ "Expense successfully updated." /*)*/;
  public static final String EXPENSE_UPDATED_NO_MAIL = /*$$(*/
      "Expense successfully updated. An error occurred while sending the mail." /*)*/;
  public static final String TIMESHEET_UPDATED = /*$$(*/ "Timesheet successfully updated." /*)*/;
  public static final String TIMESHEET_LINE_UPDATED = /*$$(*/
      "Timesheet line successfully updated." /*)*/;
  public static final String TIMESHEET_CONVERTED_PERIOD_TOTAL = /*$$(*/
      "Timesheet converted period total." /*)*/;
  public static final String TIMER_UPDATED = /*$$(*/ "Timer successfully updated." /*)*/;

  String API_LEAVE_REQUEST_UPDATED = /*$$(*/ "Leave request successfully updated." /*)*/;

  String API_LEAVE_REQUEST_UPDATED_NO_MAIL = /*$$(*/
      "Leave request successfully updated. An error occurred while sending the mail." /*)*/;

  String API_LEAVE_REQUEST_CREATE_SUCCESS = /*$$(*/
      "Leave request(s) have been correctly created." /*)*/;

  String API_LEAVE_REQUEST_CREATE_SUCCESS_WITH_ERRORS = /*$$(*/
      "Leave request(s) have been correctly created. Some requests were ignored as they were not correctly configured." /*)*/;

  String API_LEAVE_REQUEST_COMPUTE_DURATION = /*$$(*/ "Duration computed." /*)*/;

  String API_LEAVE_REQUEST_LEAVE_DAYS_TO_DATE_COMPUTATION = /*$$(*/
      "Available leave days computed." /*)*/;

  String LEAVE_REQUEST_CREATE_DURATION_ALERT = /*$$(*/
      "You exceeded the available/asked duration. Do you wish to proceed ?" /*)*/;
}
