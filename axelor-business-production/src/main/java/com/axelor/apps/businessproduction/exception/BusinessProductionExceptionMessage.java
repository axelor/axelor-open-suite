/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproduction.exception;

public final class BusinessProductionExceptionMessage {

  private BusinessProductionExceptionMessage() {}

  public static final String OPERATION_ORDER_TIMESHEET_WAITING_VALIDATION = /*$$(*/
      "There are timesheet still waiting validation on this operation order, do you want to continue anyway ?" /*)*/;
  public static final String MANUF_ORDER_TIMESHEET_WAITING_VALIDATION = /*$$(*/
      "There are timesheet still waiting validation on this manuf order, do you want to continue anyway ?" /*)*/;

  public static final String EMPLOYEE_TIME_PREFERENCE_INVALID_VALUE = /*$$(*/
      "Time preference for employee %s is not yet take into account for duration computation" /*)*/;

  public static final String WORKING_USERS_HAVE_NO_EMPLOYEE = /*$$(*/
      "Some working users have no employee associated, therefore no timesheet line will be generated for those, do you want to continue anyway ?" /*)*/;

  public static final String WORKING_USERS_EMPLOYEE_NOT_CORRECT_TIMESHEET_IMPUTATION = /*$$(*/
      "Some working employees do not have their timesheet imputation on manuf order, therefore no timesheet line will be generated for those, do you want to continue anyway ?" /*)*/;

  public static final String WORKING_USERS_EMPLOYEE_NOT_CORRECT_TIME_LOGGING = /*$$(*/
      "Some working employees have their time logging preference set to 'days' or it is not matching with their current timesheet, do you want to continue anyway ?" /*)*/;
}
